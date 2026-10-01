# Multi-Agent System Design

## Why Agents?
To take business decisions with more accuracy, 
to reduce boilerplate code, 
and speed up code delivery.

## 4 Agents We're Building

### 1. Eligibility Agent
Checks if person is eligible for benefits based on income and work history.
Scope: ELIGIBILITY ONLY

### 2. Benefit Calculator Agent
Calculates weekly benefit amount based on income.
Scope: CALCULATION ONLY

### 3. Fraud Detector Agent
Assesses fraud risk based on patterns and history.
Scope: FRAUD DETECTION ONLY

### 4. Document Validator Agent
Validates if documents are authentic and complete.
Scope: DOCUMENT VALIDATION ONLY

## How They Work

Each agent:
- Has ONE clear scope
- Makes decisions only in that scope
- Doesn't cross boundaries
- Follows the rules we specify

Agents don't talk directly. The Orchestrator (like API Gateway) routes information between them.

## Orchestrator (Like API Gateway)

The Orchestrator:
- Receives claim
- Routes to agents one by one
- Passes results between agents
- Combines final decision

Same pattern as API Gateway, but for agents instead of services.

## Agent Communication Flow

Claim comes in
  ↓
Orchestrator calls Eligibility Agent
  Result: "eligible = YES"
  ↓
Orchestrator passes result to Benefit Calculator
  Input: "Person eligible, calculate benefit"
  Result: "benefit = 50"
  ↓
Orchestrator passes result to Fraud Detector
  Input: "Benefit 50, check fraud"
  Result: "fraud_risk = 0.15"
  ↓
Orchestrator passes result to Document Validator
  Input: "Check documents"
  Result: "valid = YES"
  ↓
Orchestrator combines all results
  Final: "APPROVE $250/week, low fraud risk"
