# Multi-Agent System for UITax

## Overview
Production-grade multi-agent system using Claude AI for intelligent unemployment benefits determination.

## 4 Specialized Agents
1. Eligibility Agent - Determines if claimant qualifies
2. Benefit Calculator Agent - Calculates weekly benefit amount
3. Fraud Detector Agent - Assesses fraud risk
4. Document Validator Agent - Validates submitted documents

## Architecture
- Sequential agent orchestration (not parallel)
- Configurable YAML-based rules (agents-config.yml)
- Kafka event publishing (claim_approved, claim_denied, claim_under_review)
- Decision state machine (eligibility → calculation → fraud → validation)
- Circuit breaker + DLT error handling
- Multi-state support (GA, KS, CA with different formulas)

## Project Structure
docs/multi-agent/ - Design documentation
agent-orchestrator/ - Orchestrator service (coming Friday)
agents/ - 4 AI agents using Claude API (coming Saturday)

## Files Created
✅ DESIGN.md - Architecture overview
✅ AGENT_SPECS.md - Agent specifications with configurable rules
✅ ORCHESTRATOR_DESIGN.md - Orchestration logic & patterns
✅ README.md - This file

## Why This Approach
- Less Code - Rules in YAML, not Java
- More AI - Claude makes intelligent decisions
- Flexible - Change rules without code deployment
- Multi-State - Different rules per state
- Enterprise - Production patterns built-in
- Interview-Ready - Shows advanced system design

## Next Steps
Friday: Build Orchestrator Service (Java/Spring Boot)
Saturday: Build 4 Claude AI agents
Sunday: Deploy to Kubernetes + integration testing

## Key Takeaway
Modern backend development shifts from writing business logic code to orchestrating AI agents that make intelligent decisions based on configurable rules.
