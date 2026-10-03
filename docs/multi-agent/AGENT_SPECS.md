# Agent Specifications

## Overview
Each agent has a specific scope and makes decisions only within that scope. Agents accept JSON input, use Claude AI with configured rules, and return structured output.

**Key Principle:** Rules are loaded from configuration, not hardcoded.

---

## Configuration Management

### Agent Rules Configuration (agents-config.yml)
```yaml
# agents-config.yml - Loaded at runtime
eligibility:
  state: GA
  rules:
    min_income_12_months: 5000
    min_weeks_employed: 20
    allowed_unemployment_reasons:
      - "Employer closed business"
      - "Layoff"
      - "Temporary shutdown"
    rejected_reasons:
      - "Quit without cause"
      - "Fired for misconduct"

benefit_calculator:
  state: GA
  formula: "(highest_quarter_earnings / 26) * 0.5"
  min_weekly: 50
  max_weekly: 450
  state_formulas:
    GA: "(hq_earnings / 26) * 0.5"
    KS: "(hq_earnings / 26) * 0.55"
    CA: "(hq_earnings / 26) * 0.6"

fraud_detector:
  risk_thresholds:
    low: 0.0
    medium: 0.4
    high: 0.7
  risk_factors:
    first_time_filer: -0.1  # Negative = lower risk
    multiple_claims_same_quarter: 0.3
    doc_quality_poor: 0.2
    income_volatility_high: 0.15

document_validator:
  required_documents:
    - W2_form
    - recent_pay_stubs
    - termination_letter
  min_doc_count: 3
  date_recency_months: 12
```

---

## 1. ELIGIBILITY AGENT

### Scope
Determines eligibility based on state-specific rules loaded from configuration.

### Input Format
```json
{
  "claimant_id": "CLM-2024-001",
  "income_last_12_months": 45000,
  "weeks_employed_last_12_months": 50,
  "reason_for_unemployment": "Employer closed business",
  "state": "GA"
}
```

### Claude Prompt (Dynamic - Rules Loaded from Config)
```
You are an Eligibility Agent for unemployment benefits.

CONFIGURATION RULES (Loaded from agents-config.yml):
- Minimum income requirement: {MIN_INCOME_CONFIG}
- Minimum work weeks: {MIN_WEEKS_CONFIG}
- Allowed reasons: {ALLOWED_REASONS_CONFIG}
- State: {STATE_CONFIG}

Input:
{INPUT}

IMPORTANT: 
- Respond ONLY in JSON format
- Decision: "ELIGIBLE" or "NOT_ELIGIBLE"
- Provide reasoning based on config rules
- Do NOT make decisions about benefit amounts

Output Format:
{
  "decision": "ELIGIBLE or NOT_ELIGIBLE",
  "reasoning": "Explanation based on applied rules",
  "rules_applied": ["rule1", "rule2"],
  "confidence": 0.95,
  "flags": []
}
```

### Output Format
```json
{
  "decision": "ELIGIBLE",
  "reasoning": "Claimant meets: income $45,000 (config min: $5,000), 50 weeks (config min: 20), unemployment reason 'Employer closed' is in allowed_reasons",
  "rules_applied": ["min_income_check", "min_weeks_check", "reason_validation"],
  "confidence": 0.98,
  "flags": []
}
```

### Configuration-Driven Behavior
- Config file: `agents-config.yml` → eligibility section
- Min income pulled from: `eligibility.rules.min_income_12_months`
- Allowed reasons pulled from: `eligibility.rules.allowed_unemployment_reasons`
- State rules pulled from: `eligibility.state`

---

## 2. BENEFIT CALCULATOR AGENT

### Scope
Calculates weekly benefit using state-specific formula from configuration.

### Input Format
```json
{
  "claimant_id": "CLM-2024-001",
  "eligibility_status": "ELIGIBLE",
  "highest_quarter_earnings": 12000,
  "state": "GA"
}
```

### Claude Prompt (Dynamic - Formula Loaded from Config)
```
You are a Benefit Calculator Agent for unemployment benefits.

CONFIGURATION (From agents-config.yml):
- Formula for {STATE}: {FORMULA_CONFIG}
- Minimum weekly: {MIN_WEEKLY_CONFIG}
- Maximum weekly: {MAX_WEEKLY_CONFIG}

Input:
{INPUT}

IMPORTANT:
- Respond ONLY in JSON format
- Use the state-specific formula from config
- Apply min/max limits from config
- Calculate benefit as NUMBER (not string)
- Only if eligibility_status is ELIGIBLE

Output Format:
{
  "weekly_benefit": 230.77,
  "calculation_breakdown": "Formula used and step-by-step",
  "config_formula_used": "{FORMULA}",
  "min_max_applied": true,
  "calculation_complete": true,
  "notes": ""
}
```

### Configuration-Driven Behavior
- Formula pulled from: `benefit_calculator.state_formulas[{STATE}]`
- Min/max pulled from: `benefit_calculator.min_weekly`, `benefit_calculator.max_weekly`
- Can support multiple states with different formulas

---

## 3. FRAUD DETECTOR AGENT

### Scope
Assesses fraud risk using configurable risk factor weights.

### Input Format
```json
{
  "claimant_id": "CLM-2024-001",
  "claim_history": "First time filer",
  "document_quality": "High",
  "income_volatility": "Low",
  "claim_amount": 230.77
}
```

### Claude Prompt (Dynamic - Risk Factors Loaded from Config)
```
You are a Fraud Detector Agent for unemployment claims.

CONFIGURATION RISK FACTORS (From agents-config.yml):
{RISK_FACTORS_CONFIG}

Risk Thresholds:
- LOW: 0.0 - 0.4
- MEDIUM: 0.4 - 0.7
- HIGH: 0.7 - 1.0

Input:
{INPUT}

IMPORTANT:
- Respond ONLY in JSON format
- Calculate fraud_score using config risk factors
- Risk level based on config thresholds
- Do NOT approve/deny - just assess

Output Format:
{
  "fraud_risk_level": "LOW",
  "fraud_score": 0.12,
  "risk_factors_detected": [],
  "config_thresholds_used": true,
  "recommendation": "REVIEW"
}
```

### Configuration-Driven Behavior
- Risk factors pulled from: `fraud_detector.risk_factors`
- Thresholds pulled from: `fraud_detector.risk_thresholds`
- Dynamically weighted based on config

---

## 4. DOCUMENT VALIDATOR AGENT

### Scope
Validates documents against state-specific requirements.

### Input Format
```json
{
  "claimant_id": "CLM-2024-001",
  "documents_submitted": [
    "W2_form_2023",
    "pay_stub_recent_1",
    "pay_stub_recent_2",
    "termination_letter"
  ],
  "submission_date": "2024-10-01",
  "state": "GA"
}
```

### Claude Prompt (Dynamic - Requirements Loaded from Config)
```
You are a Document Validator Agent for unemployment claims.

CONFIGURATION REQUIREMENTS (From agents-config.yml):
- Required documents: {REQUIRED_DOCS_CONFIG}
- Minimum document count: {MIN_COUNT_CONFIG}
- Document date recency: {DATE_RECENCY_CONFIG} months

Input:
{INPUT}

IMPORTANT:
- Respond ONLY in JSON format
- Validate against config requirements
- Check document dates within recency window
- List missing documents from config requirements

Output Format:
{
  "validation_status": "COMPLETE or INCOMPLETE",
  "documents_valid": true,
  "missing_documents": [],
  "config_requirements_met": true,
  "validation_confidence": 0.95
}
```

### Configuration-Driven Behavior
- Required docs pulled from: `document_validator.required_documents`
- Min count pulled from: `document_validator.min_doc_count`
- Date validation from: `document_validator.date_recency_months`

---

## Agent Communication Protocol

All agents:
1. Accept JSON input with state parameter
2. Load rules from agents-config.yml at runtime
3. Return JSON output with config references
4. Include `config_*` fields in output (shows which config was used)
5. Do NOT cross scope boundaries
6. Process independently

The Orchestrator:
- Loads agents-config.yml once at startup
- Passes config rules to each agent
- Routes claims through agents
- Combines results in final decision

---

## Configuration File Location

```
agent-orchestrator/
  └── config/
      └── agents-config.yml
```

All agents read from this single source of truth.
