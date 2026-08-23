
----

name: data-generation
description: Generate realistic relational database seed data as SQL INSERT statements while respecting schema constraints, distributions, foreign keys, and categorical values.

----

# Data Generation
This skill should run every time a user request the generation of data.

## Constraints
1. Inspect the target table or tables with the appropriate tool.
2. If you need to inspect existing state or foreign keys for multiple tables in a step, gather all required information in a single turn using combined SELECT queries before constructing inserts.
3. Every column listed by the tool must appear in the insert statement with a valid value.
4. Every single column in every row must be populated with an explicit realistic value. Never omit a column or pass null, unless explicitly told.
5. For every timestamp/date field generate valid ISO-8601 strings. Validate them with the appropriate tool.
6. Do not use placeholder patterns, codes, or foreign key syntax as names.
7. Names must be realistic real-world names. Descriptions must be realistic, natural-language descriptions rather than placeholders or codes.
8. If the field contains categorical data, do not invent the categories, use the appropriate tool to find the list of valid values.
9. If a generated column depends on a mathematical formula derived from other values, you MUST calculate and apply the exact formula before adding it to the statement.
10. Generate as many insert statements as the user requested for each target table.
11. If the user provides a distribution, satisfy it as closely as the requested sample size permits.
12. Select foreign keys and categorical values non-sequentially, while respecting any requested distribution and avoiding obvious repetitive patterns.
13. Do not add primary keys to the inserts, the database will add them.
14. When a step requires population across multiple tables, combine all batch INSERT statements into a single script/tool call separated by semicolons rather than making separate tool calls per table.
15. Before returning the statements, verify that all requested row counts, distributions, foreign-key references, categorical constraints, and derived-value rules are satisfied.

## Rules for Dynamic Distribution:
1. When generating data for a target table, query `synthetic_distribution_rules` for that `table_name` if you have not already fetched it.
2. Interpret the retrieved JSON rules as follows:
    - `child_cardinality`: When creating child rows (e.g., `order_line` for an `order`), generate the exact number of child records per parent based on the declared `weight_percent` ratios.
    - `categorical_percent`: Distribute enum/text column values strictly following the given percentage weights.
    - `pareto_skew`: Concentrate 80% of foreign key references on the first 20% of parent records.
    - `range_uniform`: Keep numeric values strictly bounded between `min` and `max`.
3. Do NOT issue follow-up verification queries after reading the distribution rules. Apply them directly to the batch `INSERT`.

Do not insert the data, generate the insert statements.


---- 

name: data-insertion
description: Inserts a provided set of INSERT statements into the database.

----

# Data Insertion
This skill should run every time a user request the insertion of data
previously generated in the database

## Constraints
1. Inspect the target table or tables with the appropriate tool.
2. "If you need to inspect existing state or foreign keys for multiple tables in a step, gather all required information in a single turn using combined SELECT queries before constructing inserts."
3. Determine the order in which different tables must be filled based on foreign keys.
4. Do not modify, invent, or regenerate data unless explicitly requested. Execute the provided statements.
5. Populate tables in the order determined before.
6. If insertion fails, report the failing statement and error instead of silently skipping it.
7. Do not continue inserting dependent data after a prerequisite insertion fails.
8. After insertion validate the results with the appropriate tool.

## CRITICAL EXECUTION & RESPONSE RULES
1. NEVER issue `DELETE`, `TRUNCATE`, or `DROP` statements under any circumstances. You operate strictly in incremental append mode.
2. NEVER generate explicit PK `id` values in `INSERT` statements unless explicitly required for foreign key mapping in the same turn. Allow the database sequences / auto-increment columns to assign primary key IDs automatically.
3. Execute all `INSERT` statements for a step in a single batch call. Do NOT perform per-table post-validation re-queries unless an error occurs.
4. When executing INSERT statements, do not pass Primary Key values. ALWAYS append RETURNING id (or RETURNING *) to your INSERT statements to immediately receive generated IDs in a single turn without running follow-up verification queries.
5. When a step requires population across multiple tables, combine all batch INSERT statements into a single script/tool call separated by semicolons rather than making separate tool calls per table.

## FINAL RESPONSE FORMATTING SPECIFICATION
- NEVER output full database inserts.
- NEVER output Markdown tables (`| ... |`) or list full dataset rows.
- Include only a summary of your work, how you used the instructions, and validations you may have conducted.
- Keep your response strictly under 10 lines using bullet points and high-level count metrics.
- You MAY include up to 2-3 brief inline examples per entity (e.g., entity name or code) to demonstrate successful generation.