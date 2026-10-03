# Orchestrator Design

## Overview
The Orchestrator is the coordinator between agents. It:
1. Receives a claim from API Gateway
2. Calls agents in sequence (not parallel)
3. Passes results between agents
4. Combines final decision
5. Returns response to API Gateway
6. Publishes events to Kafka

Think of it like an API Gateway, but for AI agents instead of microservices.

---

## Orchestrator Flow (Sequential)

```
Claim Received
  ↓
[1] Call Eligibility Agent
  Input: claimant data
  Output: eligible = YES/NO
  ↓
[2] Call Benefit Calculator Agent (Only if eligible)
  Input: claimant data + eligibility result
  Output: weekly_benefit = amount
  ↓
[3] Call Fraud Detector Agent
  Input: claimant data + benefit amount
  Output: fraud_risk = LOW/MEDIUM/HIGH
  ↓
[4] Call Document Validator Agent
  Input: claimant data + documents
  Output: validation_status = COMPLETE/INCOMPLETE
  ↓
[5] Orchestrator Decision Logic
  Combine all agent outputs
  Make final decision: APPROVE/DENY/REVIEW
  ↓
Return Final Response
```

---

## State Machine: Decision Logic

```
IF eligibility = NOT_ELIGIBLE
  → Final Decision = DENY
  → Reason: Not eligible for benefits
  → STOP

IF eligibility = ELIGIBLE
  → Call Benefit Calculator
  → IF calculation failed
    → Final Decision = REVIEW
    → Reason: Unable to calculate benefit
    → STOP

IF fraud_risk = HIGH
  → Final Decision = REVIEW
  → Reason: High fraud risk detected
  → STOP

IF validation_status = INCOMPLETE
  → Final Decision = REVIEW
  → Reason: Missing required documents
  → STOP

IF all checks pass (eligible + calculated + low fraud + documents complete)
  → Final Decision = APPROVE
  → Benefit: ${weekly_benefit}
  → Fraud Risk: {fraud_risk_level}
```

---

## Agent Call Configuration

### Sequential Calling Strategy
- Agents are called ONE AT A TIME (not parallel)
- Each agent output becomes input for next agent
- If critical agent fails, stop and return error

### Why Sequential?
1. Efficiency: Don't calculate benefit if not eligible
2. Cost: Don't check fraud if benefits denied anyway
3. Logic: Later agents need earlier agents' results
4. Clear: Easy to debug - know exactly where failure happened

---

## Timeout & Error Handling

### Agent Call Timeout
```yaml
agent_timeouts:
  eligibility_agent: 5 seconds      # Decision-critical
  benefit_calculator_agent: 3 seconds # Simple math
  fraud_detector_agent: 5 seconds     # Complex analysis
  document_validator_agent: 3 seconds # Document checks

total_orchestrator_timeout: 20 seconds # Hard limit for entire claim
```

### Timeout Actions
```
IF agent times out
  → Log error with agent name
  → Set agent response = TIMEOUT_ERROR
  → Decision Logic: Treat as REVIEW_REQUIRED
  → Return: PENDING (manual review needed)
  → Publish event: claim.agent_timeout
```

### Agent Failure Handling
```yaml
eligibility_agent_fails:
  action: DENY
  reason: Unable to determine eligibility
  manual_review: true
  kafka_event: claim.eligibility_error

benefit_calculator_fails:
  action: REVIEW
  reason: Unable to calculate benefit amount
  manual_review: true
  kafka_event: claim.calculation_error

fraud_detector_fails:
  action: REVIEW (Don't deny - review instead)
  reason: Unable to assess fraud risk
  manual_review: true
  kafka_event: claim.fraud_check_error

document_validator_fails:
  action: REVIEW
  reason: Unable to validate documents
  manual_review: true
  kafka_event: claim.validation_error
```

---

## Input to Orchestrator

```json
{
  "claim_id": "CLM-2024-001",
  "claimant_id": "CLNT-12345",
  "state": "GA",
  "claim_data": {
    "income_last_12_months": 45000,
    "weeks_employed_last_12_months": 50,
    "reason_for_unemployment": "Employer closed business",
    "highest_quarter_earnings": 12000,
    "documents_submitted": ["W2", "pay_stubs", "termination_letter"],
    "claim_submission_date": "2024-10-01"
  },
  "request_id": "REQ-UUID-12345",
  "timestamp": "2024-10-01T19:30:00Z"
}
```

---

## Output from Orchestrator

```json
{
  "claim_id": "CLM-2024-001",
  "claimant_id": "CLNT-12345",
  "final_decision": "APPROVE",
  "decision_reason": "Claimant is eligible, documents complete, low fraud risk",
  "weekly_benefit_amount": 230.77,
  "fraud_risk_level": "LOW",
  "document_validation_status": "COMPLETE",
  "agent_results": {
    "eligibility_agent": {
      "decision": "ELIGIBLE",
      "confidence": 0.98,
      "rules_applied": ["min_income_check", "min_weeks_check"]
    },
    "benefit_calculator_agent": {
      "weekly_benefit": 230.77,
      "formula_used": "GA: (HQ/26)*0.5",
      "confidence": 0.99
    },
    "fraud_detector_agent": {
      "fraud_risk_level": "LOW",
      "fraud_score": 0.12,
      "confidence": 0.95
    },
    "document_validator_agent": {
      "validation_status": "COMPLETE",
      "confidence": 0.98
    }
  },
  "processing_time_ms": 3500,
  "orchestrator_version": "1.0",
  "timestamp": "2024-10-01T19:31:30Z",
  "request_id": "REQ-UUID-12345"
}
```

---

## Kafka Integration

### Topics Published

```yaml
claim_processed:
  event_type: "CLAIM_PROCESSED"
  payload: orchestrator_output
  partition_key: claim_id
  retention: 30 days
  schema: orchestrator_response_schema

claim_approved:
  event_type: "CLAIM_APPROVED"
  payload: {claim_id, weekly_benefit, claimant_id}
  partition_key: claimant_id

claim_denied:
  event_type: "CLAIM_DENIED"
  payload: {claim_id, reason, claimant_id}
  partition_key: claimant_id

claim_under_review:
  event_type: "CLAIM_UNDER_REVIEW"
  payload: {claim_id, reason, claimant_id}
  partition_key: claimant_id

claim_error:
  event_type: "CLAIM_ERROR"
  payload: {claim_id, error_details, failed_agent}
  partition_key: claim_id
  error_handling: retry with exponential backoff

claim_agent_timeout:
  event_type: "CLAIM_AGENT_TIMEOUT"
  payload: {claim_id, agent_name, timeout_ms}
  partition_key: claim_id
  error_handling: manual review queue
```

### Event Publishing Flow

```
Orchestrator processes claim
  ↓
Produces event to appropriate Kafka topic
  - claim_approved → Notifications service (sends approval letter)
  - claim_denied → Notifications service (sends denial letter)
  - claim_under_review → Manual Review service (assign to reviewer)
  - claim_error → Error Handler service (logs and alerts)
  - claim_agent_timeout → Manual Review service (queue for review)
```

---

## API Gateway Integration

### Request from API Gateway
```
POST /api/v1/claims/process
Content-Type: application/json

{
  "claim_id": "CLM-2024-001",
  "claim_data": {...},
  "trace_id": "trace-12345"
}
```

### Response to API Gateway
```
HTTP 200 OK
Content-Type: application/json

{
  "status": "success",
  "final_decision": "APPROVE",
  "weekly_benefit_amount": 230.77,
  "processing_time_ms": 3500
}

or

HTTP 202 ACCEPTED (for long-running claims)
{
  "status": "processing",
  "claim_id": "CLM-2024-001",
  "message": "Claim is being processed, check status later"
}
```

---

## Circuit Breaker Pattern

### Why?
If an agent is having issues (slow, returning errors), don't keep calling it.

```yaml
circuit_breaker_config:
  failure_threshold: 5        # Fail 5 times
  success_threshold: 2        # Succeed 2 times to recover
  timeout_window: 60 seconds  # Check window
  
  states:
    CLOSED:   # Normal operation - call agent
    OPEN:     # Agent failing - don't call, return error
    HALF_OPEN: # Try calling agent again

agent_circuit_breakers:
  eligibility_agent:
    state: CLOSED
    failures: 0
    
  benefit_calculator_agent:
    state: CLOSED
    failures: 0
```

---

## Dead Letter Topic (DLT)

### When?
If an event can't be published to Kafka (network issue, topic error).

```yaml
dead_letter_topic: claims_dlt

dlt_handling:
  event_fails_to_publish: true
  retry_count: 3
  retry_delay: 5 seconds
  after_max_retries: write_to_dlt
  monitoring: alert on DLT accumulation
```

---

## Monitoring & Observability

```yaml
metrics_published:
  claim_processing_time: histogram
  agent_call_duration: timer per agent
  agent_success_rate: counter per agent
  final_decision_distribution: counter (APPROVE, DENY, REVIEW)
  fraud_risk_distribution: counter (LOW, MEDIUM, HIGH)
  
alerts:
  agent_timeout: if any agent > 10 seconds
  high_error_rate: if > 5% of claims fail
  dlt_queue_growing: if events backing up
  
logs:
  all_claims: claim_id, final_decision, processing_time
  errors: agent_name, error_type, error_message
  trace_id: correlate with API Gateway requests
```

---

## Orchestrator Code Structure (Java)

```
agent-orchestrator/
  src/main/java/
    com/unemployment/orchestrator/
      OrchestrationService.java          # Main orchestrator logic
      AgentClient.java                   # Call agents (Claude API)
      KafkaEventPublisher.java           # Publish Kafka events
      CircuitBreakerManager.java         # Circuit breaker pattern
      OrchestratorConfig.java            # Load agents-config.yml
  
  config/
    agents-config.yml                    # Agent rules
    orchestrator-config.yml              # Timeouts, thresholds
    kafka-topics.yml                     # Topic definitions
```

---

## Orchestrator vs Existing Microservices

| Aspect | Current Microservices | Orchestrator |
|--------|----------------------|--------------|
| Communication | REST + Kafka | Claude API (agents) |
| Logic | Java code | Claude prompts |
| Configuration | Code + properties | YAML configs |
| Error Handling | Exception handling | Timeout + graceful fallback |
| State | Database | In-memory during processing |
| Scalability | Horizontal (replicas) | Stateless service |

---

## Integration with UITax System

The Orchestrator fits into UITax like this:

```
API Gateway
  ↓
Orchestrator Service (NEW - uses agents)
  ↓
AI Agents (Claude)
  ↓
Kafka Topics
  ↓
Notification Service (email approval/denial)
Fraud Service (escalate high-risk)
Review Service (manual review queue)
```

It replaces the logic that was previously spread across multiple services.
