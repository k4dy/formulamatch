## FormulaMatch

A REST API for finding cosmetic product substitutes by ingredient similarity. Built in Java 21 with Spring Boot and PostgreSQL.

The idea: cosmetics get discontinued, reformulated, or just aren't available where you live. The obvious move is to find something with a similar ingredient list — but INCI lists can be 30–50 entries long and comparing them by hand isn't realistic. FormulaMatch pre-computes ingredient similarity for every pair of products in its database so the query is just a lookup.

Live at [formulamatch.com](https://formulamatch.com). API docs at [formulamatch.com/swagger-ui.html](https://formulamatch.com/swagger-ui.html).

---

## Data

Product data was collected from [incidecoder.com](https://incidecoder.com) in November 2024.

| Table | Rows |
|---|---|
| products | ~100,000 |
| product_ingredients | ~2,630,000 |
| cosing_substances | ~50,000 |
| product_similarities | ~5,300,000 |

### Ingredient normalisation

incidecoder.com is community-driven — users submit products themselves. That means the same ingredient appears under dozens of different names depending on who typed it in: "Aqua", "Water", "AQUA", "eau", "Wasser". Comparing ingredient lists directly by string doesn't work.

The plan was to fix this in three stages:

**1. Canonical INCI reference from COSING.** [COSING](https://ec.europa.eu/growth/tools-databases/cosing/) is the EU's official cosmetics ingredient database with ~50,000 standardised INCI names, each with a unique numeric ID. This became the source of truth.

**2. Dictionary of popular and alternative names.** To catch all the variants that appear on real product labels, two additional sources were cross-referenced — [CosDNA](https://www.cosdna.com) and [EWG Skin Deep](https://www.ewg.org/) — to build a lookup table mapping every known synonym to a single COSING ID.

**3. Fuzzy matching with manual validation.** The actual mapping used fuzzy string matching (`rapidfuzz` in Python) against the COSING INCI names. The process started at 100% match threshold, then the threshold was gradually lowered. At each step a random sample of matched pairs was reviewed manually. As soon as errors appeared in the sample, I stepped back to the previous threshold and did a full manual review of that boundary. The goal was to catch every bad match before it contaminated the similarity scores.

This was the most time-consuming part of the project. Without it the similarity scores would be meaningless — two products with "Aqua" and "Water" at position 1 would look like they share zero ingredients.

### Similarity scoring

EU law requires cosmetics to list ingredients in descending order of concentration — so position 1 is the dominant ingredient, position 5 is still present in meaningful quantity. The first five ingredients typically make up the bulk of a product's formula.

Two products are compared by counting how many COSING IDs they share across their full ingredient list. That gives the raw `match_count`. But the more useful signal is how many of those matches fall within the top 5 — two products that share the same dominant ingredients are a much closer substitute than two that share obscure ingredients deep in the list.

This is why the API exposes a `top5Filter` parameter: it filters results to products that share at least N of the first 5 ingredients with the reference product.

A **golden match** is a special case: both products have identical first 5 ingredients and in the same order (same positions). That's as close as two different products can get — essentially the same base formula.

Product similarity is pre-computed and stored. Doing it at query time across 100k products isn't feasible.

---

## Disclaimer

The data in this database was collected in November 2024 and has not been updated since. Formulations change, products get discontinued, and user-submitted ingredient lists may contain errors.

This project is a technical concept and proof of concept only. The similarity scores and ingredient data are provided for informational and educational purposes and should not be used to draw any conclusions about product safety, efficacy, or suitability. Do not use this data as the basis for medical, dermatological, or purchasing decisions.

The author makes no representations or warranties of any kind regarding the accuracy, completeness, or fitness for any purpose of the data provided. Use at your own risk.

---

## Demo constraints

This is a demo deployment. A few things to know before using it:

**Rate limit — 100 requests per IP per 24 hours.** The counter is a sliding window, not a midnight reset. IPs that hit the limit two days in a row get permanently blocked.

**Database resets every 72 hours.** All user accounts, submitted products, and proposed changes are wiped. The core product dataset (the original 100k products) is never touched — only crowdsourced additions go.

---

## API

Register at [formulamatch.com](https://formulamatch.com) to get an API key, then pass it as `X-Api-Key: your-key`.

**Public — no key needed:**

```
GET /api/v1/products/search?q=nivea
GET /api/v1/products/{id}
GET /api/v1/products/{id}/substitutes?top5Filter=3
```

**Requires API key:**

```
POST /api/v1/submissions/products
POST /api/v1/submissions/products/{productId}/proposals
```

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.4, Spring Security |
| Database | PostgreSQL 16 |
| Auth | API key (`X-Api-Key` header), BCrypt |
| Frontend | Vanilla HTML/CSS/JS |
| Infrastructure | Docker Compose, Nginx Proxy Manager |
| API docs | Springdoc OpenAPI (Swagger UI) |

---

## Architecture

### Design decisions

**Raw SQL for the complex queries.** JPA is fine for CRUD but the similarity queries — sort by match count, filter by top-5 threshold, join across large tables — are just easier to write and tune in plain SQL. So that's what I used, with `@Query` annotations.

**API keys instead of JWT.** JWT brings a lot of moving parts (expiry, refresh, signature verification) that this project doesn't need. API keys are stateless, straightforward to implement correctly, and the same pattern used by Stripe and OpenAI. Passwords are BCrypt-hashed, keys are random UUIDs.

**Crowdsourcing with a quorum and time gaps.** A submitted product goes live when three different users submit the same data, with at least two hours between each submission. The time gap is the main anti-abuse mechanism — it stops someone from just making three accounts and pushing something through in five minutes. Matching is on normalised name, brand, and a sorted ingredient fingerprint.

**Anyone can read, only registered users can write.** Search and product pages are open with no account needed. Submissions require a key. Lowering the bar to explore the data felt more important than locking everything down.

**Deleting an account doesn't delete submissions.** A user's submission might already be part of an ongoing quorum group. If it disappears, the quorum breaks. So when an account is deleted, the submissions stay and `user_id` is set to `NULL` — the data stays valid, only the account goes.
