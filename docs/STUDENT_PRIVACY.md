# Student privacy release gate

CodingArena deliberately stores only the information needed for learning and
classroom operation. This checklist is an engineering gate, not legal advice.

Before enabling accounts for minors in production:

- Complete review with qualified privacy counsel for every launch region.
- Publish retention and deletion periods for accounts, source submissions,
  audit events, classroom memberships, and progress snapshots.
- Add verified school/parent consent flows wherever the target age or region
  requires them; until then, restrict self-service accounts to the approved age.
- Provide account export and deletion endpoints and verify backups follow the
  same retention policy.
- Keep student email optional for school-managed membership; do not collect
  birth dates, precise location, contacts, advertising identifiers, or payment
  details in the learning client.
- Never expose student source code, email, or cross-class rankings in teacher
  dashboards. Teachers may only access classes they own.
- Complete threat modeling and penetration testing for JWT handling, invite
  codes, hidden tests, Judge0 isolation, database authorization, and abuse/rate
  controls.
- Document incident response, breach notification, and school support paths.

The production release must remain blocked until each applicable item has an
owner, evidence, and sign-off.
