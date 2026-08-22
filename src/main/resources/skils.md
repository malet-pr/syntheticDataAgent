
----

name: data-generation
description: Generate realistic relational database seed data as SQL INSERT statements while respecting schema constraints, distributions, foreign keys, and categorical values.

----

# Data Generation
This skill should run every time a user request the generation of data.

## Constraints
1. Inspect the target table or tables with the appropriate tool.
2. Every column listed by the tool must appear in the insert statement with a valid value.
3. Every single column in every row must be populated with an explicit realistic value. Never omit a column or pass null, unless explicitly told.
4. For every timestamp/date field generate valid ISO-8601 strings. Validate them with the appropriate tool.
5. Do not use placeholder patterns, codes, or foreign key syntax as names.
6. Names must be realistic real-world names. Descriptions must be realistic, natural-language descriptions rather than placeholders or codes.
7. If the field contains categorical data, do not invent the categories, use the appropriate tool to find the list of valid values.
8. If a generated column depends on a mathematical formula derived from other values, you MUST calculate and apply the exact formula before adding it to the statement.
9. Generate as many insert statements as the user requested for each target table.
10. If the user provides a distribution, satisfy it as closely as the requested sample size permits.
11. Select foreign keys and categorical values non-sequentially, while respecting any requested distribution and avoiding obvious repetitive patterns.
12. Do not add primary keys to the inserts, the database will add them.
13. Before returning the statements, verify that all requested row counts, distributions, foreign-key references, categorical constraints, and derived-value rules are satisfied.

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
2. Determine the order in which different tables must be filled based on foreign keys.
3. Do not modify, invent, or regenerate data unless explicitly requested. Execute the provided statements.
4. Populate tables in the order determined before.
5. If insertion fails, report the failing statement and error instead of silently skipping it.
6. Do not continue inserting dependent data after a prerequisite insertion fails.
7. After insertion validate the results with the appropriate tool.