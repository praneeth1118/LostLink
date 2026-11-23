# LostLink

LostLink is an intelligent matching logic system written in Java designed for Lost & Found item reports.

## Features

- **Multi-Attribute Weighted Scoring**: Computes match percentage based on weighted similarities:
  - **Description**: 30%
  - **Category**: 15%
  - **Brand**: 15%
  - **Colour**: 15%
  - **Location**: 15%
  - **Date Proximity**: 10%
- **String Normalization & Levenshtein Distance**: Handles typos, casing, and Unicode variations gracefully when comparing textual descriptions.
- **Temporal Proximity Evaluation**: Accounts for report dates with proximity scoring that decays over a 30-day window.
- **Detailed Match Breakdown**: Provides explanations and breakdown reasons for each comparison.

## Getting Started

### Prerequisites
- Java Development Kit (JDK 8 or higher)

### Compile and Run

```bash
javac LostLinkMatchingLogic.java
java LostLinkMatchingLogic
```
