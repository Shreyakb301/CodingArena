package com.codingarena.content

import com.codingarena.domain.model.BehavioralCategory
import com.codingarena.domain.model.BehavioralCategory.AMBIGUITY
import com.codingarena.domain.model.BehavioralCategory.COMMUNICATION
import com.codingarena.domain.model.BehavioralCategory.CONFLICT
import com.codingarena.domain.model.BehavioralCategory.FAILURE_LEARNING
import com.codingarena.domain.model.BehavioralCategory.LEADERSHIP
import com.codingarena.domain.model.BehavioralCategory.OWNERSHIP
import com.codingarena.domain.model.BehavioralCategory.PRIORITIZATION
import com.codingarena.domain.model.BehavioralCategory.PROJECT_IMPACT
import com.codingarena.domain.model.BehavioralCategory.TEAMWORK
import com.codingarena.domain.model.BehavioralExercise
import com.codingarena.domain.model.ChoiceRating
import com.codingarena.domain.model.ChoiceRating.REASONABLE
import com.codingarena.domain.model.ChoiceRating.STRONG
import com.codingarena.domain.model.ChoiceRating.WEAK
import com.codingarena.domain.model.InterviewExerciseMode
import com.codingarena.domain.model.MockInterview
import com.codingarena.domain.model.MockInterviewDecision
import com.codingarena.domain.model.PracticeDifficulty
import com.codingarena.domain.model.PracticeDifficulty.DEVELOPING
import com.codingarena.domain.model.RatedChoice
import com.codingarena.domain.model.searchOrder
import com.codingarena.domain.model.StarStage
import com.codingarena.domain.model.StarStage.ACTION
import com.codingarena.domain.model.StarStage.REFLECTION
import com.codingarena.domain.model.StarStage.RESULT
import com.codingarena.domain.model.StarStage.SITUATION
import com.codingarena.domain.model.StarStage.TASK
import com.codingarena.domain.model.StarStep
import com.codingarena.domain.model.StarWorkout
import com.codingarena.domain.model.TechCommExerciseKind
import com.codingarena.domain.model.TechCommExerciseKind.CLARIFY_PROBLEM
import com.codingarena.domain.model.TechCommExerciseKind.DISCUSS_TRADEOFFS
import com.codingarena.domain.model.TechCommExerciseKind.EXPLAIN_APPROACH
import com.codingarena.domain.model.TechCommExerciseKind.EXPLAIN_EDGE_CASES
import com.codingarena.domain.model.TechCommItem

internal fun choice(id: String, text: String, rating: ChoiceRating, feedback: String) =
    RatedChoice(id, text, rating, feedback)

internal fun star(stage: StarStage, prompt: String, choices: List<RatedChoice>) = StarStep(stage, prompt, choices)

/**
 * Behavioral, technical-communication, and mock-interview content for the
 * Interview tab.
 *
 * Every [StarWorkout] step - and every single-decision [BehavioralExercise]
 * or [TechCommItem] - is authored with exactly one [STRONG], one
 * [REASONABLE], and one [WEAK] choice, verified by
 * `InterviewContentTest`. Distractors are written to be individually
 * plausible - a REASONABLE choice is genuinely defensible, just incomplete;
 * a WEAK choice is a mistake a real candidate could make, not a strawman.
 *
 * Content breadth is intentionally staged: every [BehavioralCategory] has a
 * full STAR workout, but [InterviewExerciseMode.IMPROVE_AN_ANSWER],
 * [InterviewExerciseMode.SPOT_RED_FLAG], and [InterviewExerciseMode.HANDLE_FOLLOWUPS] are
 * authored for a growing subset of categories rather than all nine at once -
 * see [InterviewContent.behavioralExercises]'s doc comment for exactly which.
 */
object InterviewContent {

    // ---------------------------------------------------------------- STAR workouts

    private val leadershipStar = StarWorkout(
        id = "leadership-mentoring-star",
        commonQuestion = "Tell me about a time you led a team through a difficult project.",
        category = LEADERSHIP,
        scenario = "During a critical migration project, you noticed the rollout was falling behind schedule " +
            "because two teams were not coordinating well, even though you had no formal authority over either team.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "leadership-star-situation-a",
                        "I describe the specific migration, the two teams involved, and the concrete signal that made me realize coordination had broken down (missed a shared deadline twice in a row).",
                        STRONG,
                        "This grounds the story in specific, verifiable facts an interviewer can picture and ask follow-up questions about, rather than a vague generalization.",
                    ),
                    choice(
                        "leadership-star-situation-b",
                        "I say the project was falling behind and teams weren't talking to each other.",
                        REASONABLE,
                        "This states the problem but skips the concrete detail (which teams, what signal, what deadline) that makes a Situation memorable and credible.",
                    ),
                    choice(
                        "leadership-star-situation-c",
                        "I explain that our whole department had communication problems in general.",
                        WEAK,
                        "This drifts into a broad organizational complaint instead of one specific, ownable situation - it also risks sounding like you're blaming the company rather than describing a moment you acted in.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task - what you decided needed to happen?",
                listOf(
                    choice(
                        "leadership-star-task-a",
                        "I say I decided someone needed to create a single shared view of both teams' work and get them talking directly, and that I chose to be that person even though it wasn't formally my job.",
                        STRONG,
                        "This shows initiative and a clear, self-chosen responsibility - exactly what 'Task' should capture in a leadership story without authority.",
                    ),
                    choice(
                        "leadership-star-task-b",
                        "I say my manager asked me to help get the teams aligned.",
                        REASONABLE,
                        "This is honest, but it shifts the initiative to your manager - the interviewer is trying to hear about a task you took on, not one assigned to you.",
                    ),
                    choice(
                        "leadership-star-task-c",
                        "I say the task was to make sure the migration succeeded no matter what.",
                        WEAK,
                        "This is too broad to be a real task - 'make it succeed' doesn't describe a specific responsibility you took on, so it won't hold up under a follow-up question.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action - what you actually did?",
                listOf(
                    choice(
                        "leadership-star-action-a",
                        "I describe setting up a twice-weekly 15-minute sync between the two teams, creating a shared tracker both teams could see, and personally following up with each lead individually when something looked stuck.",
                        STRONG,
                        "Specific, repeatable actions like this are exactly what interviewers want - they show a concrete method, not just good intentions.",
                    ),
                    choice(
                        "leadership-star-action-b",
                        "I say I talked to both team leads and encouraged them to communicate more.",
                        REASONABLE,
                        "This is true but vague - 'encouraged them to communicate' doesn't tell the interviewer what you actually built or changed.",
                    ),
                    choice(
                        "leadership-star-action-c",
                        "I say I took over decision-making for both teams until the project was back on track.",
                        WEAK,
                        "Without formal authority, describing yourself as 'taking over' both teams reads as overstepping rather than leading - it also isn't very believable without more explanation.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "leadership-star-result-a",
                        "I say the migration finished only four days later than the original date instead of the three weeks it was on pace to slip, and both team leads asked to keep the shared sync going afterward.",
                        STRONG,
                        "A specific, comparative outcome (four days vs. three weeks) plus a sign the change stuck (leads wanted to keep it) makes the impact concrete and credible.",
                    ),
                    choice(
                        "leadership-star-result-b",
                        "I say the migration finished successfully not long after.",
                        REASONABLE,
                        "This confirms a good outcome but gives no sense of scale - 'not long after' could mean anything, so it's hard for the interviewer to judge the actual impact.",
                    ),
                    choice(
                        "leadership-star-result-c",
                        "I say the project was a huge success and everyone was very happy.",
                        WEAK,
                        "This is generic praise with no measurable detail - it reads as an unsubstantiated claim rather than a result you can actually stand behind.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection - what you'd take forward?",
                listOf(
                    choice(
                        "leadership-star-reflection-a",
                        "I say I learned that visibility, not authority, was usually the real blocker, and I now default to setting up a shared view of the work early on any cross-team effort.",
                        STRONG,
                        "This names a specific, transferable lesson and shows it already changed how you operate - exactly what 'Reflection' should demonstrate.",
                    ),
                    choice(
                        "leadership-star-reflection-b",
                        "I say I learned that communication is really important in cross-team projects.",
                        REASONABLE,
                        "This is true but generic enough to follow almost any story - it doesn't show a specific insight this particular experience gave you.",
                    ),
                    choice(
                        "leadership-star-reflection-c",
                        "I say I'd probably do the same thing again if it came up.",
                        WEAK,
                        "This doesn't name anything you actually learned - a reflection that could apply to any outcome, good or bad, misses the point of the stage.",
                    ),
                ),
            ),
        ),
    )

    private val conflictStar = StarWorkout(
        id = "conflict-disagreement-star",
        commonQuestion = "Tell me about a time you had a disagreement with a coworker and how you resolved it.",
        category = CONFLICT,
        scenario = "You strongly disagreed with a senior teammate's technical approach on a project you were both responsible for delivering.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "conflict-star-situation-a",
                        "I describe the specific disagreement: a senior teammate wanted to reuse an existing service for a new feature, and I believed it would create a scaling problem within a few months, based on traffic patterns I'd already seen.",
                        STRONG,
                        "Naming the specific technical disagreement and the evidence behind your position gives the interviewer something concrete to evaluate, not just 'we disagreed.'",
                    ),
                    choice(
                        "conflict-star-situation-b",
                        "I say a senior teammate and I disagreed about the technical approach for a feature.",
                        REASONABLE,
                        "This names the disagreement but leaves out what it was actually about, which is the part that shows real technical judgment.",
                    ),
                    choice(
                        "conflict-star-situation-c",
                        "I say I often disagree with teammates about technical decisions.",
                        WEAK,
                        "This generalizes into a pattern rather than describing one specific moment, which is what a STAR situation needs to stay credible.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "conflict-star-task-a",
                        "I say my task was to make sure the concern got a fair hearing before the decision was locked in, without just overriding a more senior teammate or staying quiet to avoid friction.",
                        STRONG,
                        "This frames the task around a real tension - being heard without being dismissive or passive - which is exactly what interviewers are probing for in conflict questions.",
                    ),
                    choice(
                        "conflict-star-task-b",
                        "I say I needed to convince him my approach was better.",
                        REASONABLE,
                        "This frames it as needing to 'win,' which misses the collaborative framing interviewers usually want to hear in a disagreement story.",
                    ),
                    choice(
                        "conflict-star-task-c",
                        "I say I needed to decide whether to escalate to our manager right away.",
                        WEAK,
                        "Jumping straight to escalation as the task skips the step of trying to resolve it directly, which is usually what's actually being tested here.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "conflict-star-action-a",
                        "I describe asking for 20 minutes to walk through the traffic data together, proposing we prototype both approaches against a realistic load test, and agreeing in advance on what result would settle the question.",
                        STRONG,
                        "This shows a structured, evidence-based way of resolving disagreement - proposing a shared test rather than arguing opinions is a strong, specific action.",
                    ),
                    choice(
                        "conflict-star-action-b",
                        "I say I explained my concerns clearly and listened to his reasoning too.",
                        REASONABLE,
                        "This is reasonable interpersonal behavior, but it doesn't describe a concrete method for actually resolving the disagreement, just for discussing it.",
                    ),
                    choice(
                        "conflict-star-action-c",
                        "I say I deferred to his experience since he was more senior.",
                        WEAK,
                        "This avoids the conflict rather than resolving it, and it doesn't show the technical judgment or backbone the question is trying to surface.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "conflict-star-result-a",
                        "I say the load test showed my concern was valid at 3x current traffic, so we adjusted the design together, and it shipped without the scaling issue when traffic grew later that quarter.",
                        STRONG,
                        "A verifiable outcome (the test result) plus a real consequence avoided (no scaling issue later) makes this result concrete and believable.",
                    ),
                    choice(
                        "conflict-star-result-b",
                        "I say we ended up going with my approach after the discussion.",
                        REASONABLE,
                        "This states the outcome but skips how you know it was the right call, which is the detail that makes a result persuasive.",
                    ),
                    choice(
                        "conflict-star-result-c",
                        "I say the disagreement eventually blew over and we moved on.",
                        WEAK,
                        "This suggests the conflict just faded rather than being resolved, which undercuts the story - interviewers want to hear a definite outcome, not an anticlimax.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "conflict-star-reflection-a",
                        "I say I learned that proposing a quick shared test settles technical disagreements faster and less personally than arguing positions, and I still default to that when I disagree with someone now.",
                        STRONG,
                        "This names a specific, reusable technique the experience taught you and confirms you've kept using it - strong evidence of real reflection.",
                    ),
                    choice(
                        "conflict-star-reflection-b",
                        "I say I learned it's important to speak up even when someone is more senior than you.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture anything specific to how this particular disagreement got resolved.",
                    ),
                    choice(
                        "conflict-star-reflection-c",
                        "I say I learned that things usually work out if you stay calm.",
                        WEAK,
                        "This is vague reassurance rather than an actual lesson - it doesn't say what you'd do differently or keep doing because of this experience.",
                    ),
                ),
            ),
        ),
    )

    private val failureLearningStar = StarWorkout(
        id = "failure-learning-missed-deadline-star",
        commonQuestion = "Tell me about a time you failed. What did you learn?",
        category = FAILURE_LEARNING,
        scenario = "You committed to a deadline that you ended up missing, and it affected a launch other people were counting on.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "failure-star-situation-a",
                        "I describe committing to finish a specific piece of the launch by a fixed date, underestimating how much work an integration would take, and realizing with about a week left that I would miss it.",
                        STRONG,
                        "This names the specific commitment and the moment you realized it was at risk, which grounds the failure in something concrete rather than a vague setback.",
                    ),
                    choice(
                        "failure-star-situation-b",
                        "I say I missed an important deadline on a launch.",
                        REASONABLE,
                        "This states the failure but skips the specifics of what was committed and why it slipped, which is what makes the story feel honest rather than generic.",
                    ),
                    choice(
                        "failure-star-situation-c",
                        "I say deadlines are sometimes just unrealistic and get missed.",
                        WEAK,
                        "This frames the miss as inevitable rather than something you're taking ownership of, which is the opposite of what a Failure & Learning answer needs to show.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "failure-star-task-a",
                        "I say once I realized I'd miss it, my task was to tell the people depending on my part as early as possible and give them an honest, revised estimate rather than staying quiet and hoping to catch up.",
                        STRONG,
                        "This frames the task around early, honest communication under pressure, which is exactly the behavior interviewers are trying to surface with a failure question.",
                    ),
                    choice(
                        "failure-star-task-b",
                        "I say my task was to work extra hours to try to still hit the original date.",
                        REASONABLE,
                        "This is a reasonable instinct, but framing the task purely as 'work harder' skips the more important task of communicating the risk to people relying on you.",
                    ),
                    choice(
                        "failure-star-task-c",
                        "I say my task was to figure out who was really at fault for the underestimate.",
                        WEAK,
                        "Focusing on assigning blame, even to yourself in a self-critical way, isn't a task - it distracts from the more useful question of what you actually did next.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "failure-star-action-a",
                        "I describe flagging the risk to my manager and the dependent team three days before the deadline with a specific revised date and what was left to do, instead of waiting until the original date arrived.",
                        STRONG,
                        "Naming exactly when and what you communicated shows the concrete, proactive action that turns a failure story into a story about good judgment under pressure.",
                    ),
                    choice(
                        "failure-star-action-b",
                        "I say I let my manager know I was going to be late.",
                        REASONABLE,
                        "This shows honesty, but it's missing the detail (how early, what information you gave) that shows you handled the failure well rather than just reporting it.",
                    ),
                    choice(
                        "failure-star-action-c",
                        "I say I kept working quietly and hoped to finish just a little late without anyone noticing.",
                        WEAK,
                        "This describes avoiding the conversation rather than handling it, which is usually the actual failure interviewers are listening for in this kind of question.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "failure-star-result-a",
                        "I say the dependent team was able to adjust their own plan with three days' notice, the feature shipped four days late with no other impact, and my manager told me the early warning was what made it manageable.",
                        STRONG,
                        "This shows the failure happened, but the outcome was contained because of how it was handled - a specific, credible result that doesn't pretend the mistake didn't happen.",
                    ),
                    choice(
                        "failure-star-result-b",
                        "I say it worked out fine in the end and nobody seemed too upset.",
                        REASONABLE,
                        "This suggests things were okay but doesn't explain why - the interviewer can't tell whether that was because of something you did or just luck.",
                    ),
                    choice(
                        "failure-star-result-c",
                        "I say the launch happened on time anyway so it wasn't really a big deal.",
                        WEAK,
                        "This undercuts the whole premise of the story - if you're using it as a failure example, the result shouldn't imply there was no real consequence.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "failure-star-reflection-a",
                        "I say I learned to build in a buffer for unknowns on any estimate involving an integration I haven't done before, and I now flag risk the moment I see it rather than the moment I'm sure.",
                        STRONG,
                        "This names two specific, lasting changes to how you estimate and communicate - concrete evidence the failure actually changed your behavior.",
                    ),
                    choice(
                        "failure-star-reflection-b",
                        "I say I learned to communicate earlier when something is at risk.",
                        REASONABLE,
                        "This is a fair takeaway, but on its own it's fairly generic - pairing it with a concrete change to how you estimate would make it stronger.",
                    ),
                    choice(
                        "failure-star-reflection-c",
                        "I say I learned that mistakes happen and you just have to move past them.",
                        WEAK,
                        "This is true but doesn't name anything specific you'd actually do differently, so it reads as deflection rather than genuine reflection.",
                    ),
                ),
            ),
        ),
    )

    private val ownershipStar = StarWorkout(
        id = "ownership-accountability-star",
        commonQuestion = "Describe a situation where you took responsibility for a problem that wasn't directly your fault.",
        category = OWNERSHIP,
        scenario = "A configuration change you pushed to production caused a billing calculation error that " +
            "overcharged a subset of customers for two days before anyone noticed.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "ownership-star-situation-a",
                        "I describe the specific change I pushed - a rounding logic update to the billing service - and how it was discovered: a customer support ticket flagging an overcharge, two days after the deploy.",
                        STRONG,
                        "This grounds the story in the exact change and the exact discovery moment, which makes it clear you're describing something you can be held accountable for, not a vague system issue.",
                    ),
                    choice(
                        "ownership-star-situation-b",
                        "I say a change I made caused a billing bug that overcharged some customers.",
                        REASONABLE,
                        "This owns the mistake but skips the specifics of what the change was and how it surfaced, which is what makes an ownership story feel concrete rather than generic.",
                    ),
                    choice(
                        "ownership-star-situation-c",
                        "I say our billing system had some issues around that time.",
                        WEAK,
                        "Describing it as a system issue rather than your own change quietly shifts ownership away from you, which undercuts the entire point of a story about accountability.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task - what you decided needed to happen?",
                listOf(
                    choice(
                        "ownership-star-task-a",
                        "I say my task was to own the fix and the customer impact end-to-end myself, rather than routing it entirely to the on-call team, since it was my change that caused it.",
                        STRONG,
                        "Explicitly choosing to own both the technical fix and the customer fallout is exactly the behavior an ownership question is trying to surface.",
                    ),
                    choice(
                        "ownership-star-task-b",
                        "I say my task was to help the on-call engineer figure out what had gone wrong.",
                        REASONABLE,
                        "This is a helpful instinct, but framing yourself as support rather than the owner understates the responsibility you actually had for a bug you caused.",
                    ),
                    choice(
                        "ownership-star-task-c",
                        "I say my task was to figure out whether the bug was really my fault or a pre-existing issue in the billing service.",
                        WEAK,
                        "Spending the task stage determining blame rather than fixing the problem signals defensiveness instead of the accountability this kind of question is looking for.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action - what you actually did?",
                listOf(
                    choice(
                        "ownership-star-action-a",
                        "I describe rolling back the change within the hour, identifying exactly which customers were overcharged by cross-referencing the billing logs, and personally drafting the refund and apology communication with the support team.",
                        STRONG,
                        "Concrete, sequenced actions like this - rollback, precise impact analysis, owning the customer communication - show real end-to-end accountability, not just a technical fix.",
                    ),
                    choice(
                        "ownership-star-action-b",
                        "I say I worked with the team to fix the bug and helped issue refunds to affected customers.",
                        REASONABLE,
                        "This describes the right outcome but stays vague about which parts you personally drove, which is the detail that shows ownership rather than participation.",
                    ),
                    choice(
                        "ownership-star-action-c",
                        "I say I fixed the underlying bug and let the support team handle contacting customers on their own.",
                        WEAK,
                        "Handing off the customer-facing part of your own mistake to another team reads as owning only the convenient half of the problem.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "ownership-star-result-a",
                        "I say all 340 affected customers were refunded within 48 hours with a personal note explaining what happened, and the post-incident review led to a mandatory second reviewer on any billing logic change.",
                        STRONG,
                        "A specific customer count, a fast resolution window, and a lasting process change together make this a credible, complete result rather than a vague 'it got fixed.'",
                    ),
                    choice(
                        "ownership-star-result-b",
                        "I say the refunds went out and the issue was resolved without further complaints.",
                        REASONABLE,
                        "This confirms a good outcome but gives no scale or detail, so the interviewer can't judge how significant the impact or the fix actually was.",
                    ),
                    choice(
                        "ownership-star-result-c",
                        "I say the bug got fixed and things went back to normal.",
                        WEAK,
                        "This glosses over the customer impact entirely, which is the part of an ownership story an interviewer most wants to hear was handled well.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection - what you'd take forward?",
                listOf(
                    choice(
                        "ownership-star-reflection-a",
                        "I say I learned that owning the customer-facing fallout, not just the code fix, is what actually rebuilds trust, and I now push for a second reviewer on any change touching billing logic.",
                        STRONG,
                        "This names a specific, lasting process change tied directly to the mistake, which is strong evidence the lesson actually stuck.",
                    ),
                    choice(
                        "ownership-star-reflection-b",
                        "I say I learned to be more careful when making changes to sensitive systems like billing.",
                        REASONABLE,
                        "This is a fair takeaway but generic enough to follow almost any incident story, without naming what you'd concretely do differently.",
                    ),
                    choice(
                        "ownership-star-reflection-c",
                        "I say I learned that these things can happen to anyone.",
                        WEAK,
                        "This normalizes the mistake instead of naming a personal lesson, which reads as deflection rather than genuine reflection.",
                    ),
                ),
            ),
        ),
    )

    private val teamworkStar = StarWorkout(
        id = "teamwork-support-star",
        commonQuestion = "Tell me about a time you helped a struggling teammate.",
        category = TEAMWORK,
        scenario = "Your team of four engineers was midway through a shared sprint when one teammate's tasks " +
            "started stalling because they were quietly overwhelmed by a personal issue, putting the team's " +
            "overall sprint commitment at risk.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "teamwork-star-situation-a",
                        "I describe the specific sprint, that a particular teammate's three assigned tickets had stalled for a week, and that I noticed it not because they said anything but because their daily updates had become vague and short.",
                        STRONG,
                        "Naming the specific signal you noticed (stalled tickets, vague updates) shows genuine attentiveness to a teammate rather than a generic claim of noticing they were struggling.",
                    ),
                    choice(
                        "teamwork-star-situation-b",
                        "I say a teammate on my team was falling behind on their work and it was putting the sprint at risk.",
                        REASONABLE,
                        "This states the problem but skips the specific detail of how you noticed, which is what makes the story feel observed rather than assumed after the fact.",
                    ),
                    choice(
                        "teamwork-star-situation-c",
                        "I say sometimes people on teams struggle to keep up with their work.",
                        WEAK,
                        "This generalizes into a pattern rather than describing one specific moment, which is what a STAR situation needs to stay credible.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "teamwork-star-task-a",
                        "I say my task was to figure out how to help without putting them on the spot in front of the team or making it feel like I was reporting them to our manager.",
                        STRONG,
                        "This frames the task around a real interpersonal tension - offering support without embarrassment or escalation - which is exactly what teamwork questions are probing for.",
                    ),
                    choice(
                        "teamwork-star-task-b",
                        "I say my task was to pick up some of their tickets so the sprint would still finish on time.",
                        REASONABLE,
                        "This is a fair instinct, but framing the task purely around sprint output skips the more important task of supporting the teammate as a person.",
                    ),
                    choice(
                        "teamwork-star-task-c",
                        "I say my task was to tell our manager that the teammate wasn't pulling their weight.",
                        WEAK,
                        "Escalating immediately, without first trying to understand or support the teammate directly, is the opposite of the collaborative instinct this question is testing for.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "teamwork-star-action-a",
                        "I describe messaging them privately to ask how they were doing, learning they were dealing with a family emergency, then quietly taking on one of their three tickets myself and pairing with them on a second so they only had to fully own one.",
                        STRONG,
                        "A private check-in followed by a specific, proportionate way of sharing the load shows real teamwork - support that's tactful and concrete, not just well-intentioned.",
                    ),
                    choice(
                        "teamwork-star-action-b",
                        "I say I offered to take on some of their tickets so the team could still hit the deadline.",
                        REASONABLE,
                        "This is genuinely helpful, but it skips the interpersonal step of checking in first, which is what makes the support feel personal rather than purely transactional.",
                    ),
                    choice(
                        "teamwork-star-action-c",
                        "I say I finished their tickets myself over the weekend without telling them, so the sprint would still be on track.",
                        WEAK,
                        "Doing the work silently, without the teammate's knowledge, protects the sprint number but removes their agency and can feel undermining once they find out.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "teamwork-star-result-a",
                        "I say the sprint finished on time, the teammate later told me privately that the quiet support meant a lot during a hard week, and our team adopted a habit of flagging early if someone's tickets stall for more than two days.",
                        STRONG,
                        "A completed sprint, a personal acknowledgment from the teammate, and a lasting team habit together make this a concrete, well-rounded result.",
                    ),
                    choice(
                        "teamwork-star-result-b",
                        "I say the sprint finished on time and the teammate was back to normal the following week.",
                        REASONABLE,
                        "This confirms things worked out but doesn't show any lasting effect from how you handled it, which is what makes a teamwork result memorable.",
                    ),
                    choice(
                        "teamwork-star-result-c",
                        "I say everything worked out fine in the end.",
                        WEAK,
                        "This is generic reassurance with no measurable or specific detail, so it reads as an unsubstantiated claim rather than an actual result.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "teamwork-star-reflection-a",
                        "I say I learned that a stalled ticket is often a signal worth a private check-in before it becomes a performance conversation, and I now treat quiet, vague updates as an early flag on any team I'm on.",
                        STRONG,
                        "This names a specific, transferable signal you now watch for and confirms you've kept using it - strong evidence of real reflection rather than a one-off nice gesture.",
                    ),
                    choice(
                        "teamwork-star-reflection-b",
                        "I say I learned it's important to support teammates when they're struggling.",
                        REASONABLE,
                        "This is true but generic enough to follow almost any story - it doesn't capture the specific insight this particular situation taught you.",
                    ),
                    choice(
                        "teamwork-star-reflection-c",
                        "I say I learned that everyone goes through hard times sometimes.",
                        WEAK,
                        "This is an observation about people in general, not a lesson about what you'd actually do differently, which misses the point of the stage.",
                    ),
                ),
            ),
        ),
    )

    private val ambiguityStar = StarWorkout(
        id = "ambiguity-scoping-star",
        commonQuestion = "Tell me about a time you had to solve a problem with unclear or ambiguous requirements.",
        category = AMBIGUITY,
        scenario = "You were asked to \"improve onboarding\" for new users with no further direction - no metrics, " +
            "no deadline, and no clear definition of what \"improve\" meant.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "ambiguity-star-situation-a",
                        "I describe the exact request - a one-line ask from a product lead to \"improve onboarding\" - and that when I asked what specifically was wrong with it, the answer was just that new-user retention \"felt low.\"",
                        STRONG,
                        "Quoting the actual vague ask and the follow-up you got makes the ambiguity concrete and verifiable, rather than just claiming a request was unclear.",
                    ),
                    choice(
                        "ambiguity-star-situation-b",
                        "I say I was asked to improve onboarding without much detail on what that meant.",
                        REASONABLE,
                        "This states the ambiguity but skips the specific wording and context that shows just how open-ended the request actually was.",
                    ),
                    choice(
                        "ambiguity-star-situation-c",
                        "I say I often get asked to work on things without a lot of direction.",
                        WEAK,
                        "This generalizes into a pattern rather than describing one specific ambiguous request, which is what a STAR situation needs to stay credible.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "ambiguity-star-task-a",
                        "I say my task was to turn a vague ask into a specific, scoped problem before writing any code, rather than guessing at what \"improve\" meant and building the wrong thing.",
                        STRONG,
                        "This frames the task around scoping the ambiguity itself, which is exactly the skill an ambiguity question is trying to surface.",
                    ),
                    choice(
                        "ambiguity-star-task-b",
                        "I say my task was to look into onboarding and find something worth improving.",
                        REASONABLE,
                        "This is directionally right but stays as open-ended as the original ask, without describing how you planned to actually narrow it down.",
                    ),
                    choice(
                        "ambiguity-star-task-c",
                        "I say my task was to make onboarding better as quickly as possible.",
                        WEAK,
                        "Framing speed as the goal, before the problem is even scoped, risks building the wrong thing quickly rather than the right thing at all.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "ambiguity-star-action-a",
                        "I describe pulling the funnel data myself, finding a specific step where 40% of new users dropped off, proposing that as the concrete target to the product lead, and getting a one-sentence confirmation before starting any work.",
                        STRONG,
                        "Turning ambiguity into a specific, data-backed target and confirming it before building anything is exactly the concrete method interviewers want to hear.",
                    ),
                    choice(
                        "ambiguity-star-action-b",
                        "I say I looked at some user feedback and picked a part of onboarding that seemed like it needed work.",
                        REASONABLE,
                        "This is a reasonable starting point, but relying on impression rather than data leaves the scoping decision harder to defend if questioned.",
                    ),
                    choice(
                        "ambiguity-star-action-c",
                        "I say I picked the part of onboarding I personally found most confusing and started redesigning it.",
                        WEAK,
                        "Scoping the problem based on personal opinion, without checking it against any data or the stakeholder, risks solving a problem nobody actually asked about.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "ambiguity-star-result-a",
                        "I say narrowing it to that one drop-off step let me ship a fix in a week instead of a month, and completion at that step improved from 60% to 78% within two weeks of launch.",
                        STRONG,
                        "A specific before-and-after metric plus a fast delivery timeline makes the impact of scoping the ambiguity concrete and measurable.",
                    ),
                    choice(
                        "ambiguity-star-result-b",
                        "I say the change I made ended up helping onboarding completion improve somewhat.",
                        REASONABLE,
                        "This confirms a positive outcome but gives no sense of scale, so the interviewer can't judge how meaningful the improvement actually was.",
                    ),
                    choice(
                        "ambiguity-star-result-c",
                        "I say I made several changes to onboarding and it seemed to help overall.",
                        WEAK,
                        "This describes multiple untracked changes with a vague outcome, which undercuts the story's earlier emphasis on scoping to one specific, measurable target.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "ambiguity-star-reflection-a",
                        "I say I learned that with an ambiguous ask, finding the data-backed narrow version of the problem is worth doing before any other work, and I now ask for a specific metric before scoping vague requests.",
                        STRONG,
                        "This names a specific, repeatable habit the experience taught you and confirms you still apply it, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "ambiguity-star-reflection-b",
                        "I say I learned to ask more questions when a request isn't very clear.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific scoping technique this particular experience taught you.",
                    ),
                    choice(
                        "ambiguity-star-reflection-c",
                        "I say I learned that ambiguity is just part of the job.",
                        WEAK,
                        "This is true but doesn't name anything you'd actually do differently, so it reads as acceptance rather than an actual lesson.",
                    ),
                ),
            ),
        ),
    )

    private val prioritizationStar = StarWorkout(
        id = "prioritization-competing-deadlines-star",
        commonQuestion = "Describe a time you had to prioritize competing deadlines.",
        category = PRIORITIZATION,
        scenario = "You were the only backend engineer available when three separate stakeholders each asked for " +
            "their request to be done first in the same week: a critical bug fix, a feature needed for a " +
            "prospective client's demo, and a routine data migration with a soft deadline.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "prioritization-star-situation-a",
                        "I describe the exact three asks landing within a single day - a critical bug fix from support, a feature needed for a client demo in four days, and a data migration with a soft two-week deadline - with each stakeholder telling me theirs was the priority.",
                        STRONG,
                        "Naming all three specific requests and their real constraints gives the interviewer the exact information they need to judge how you weighed them against each other.",
                    ),
                    choice(
                        "prioritization-star-situation-b",
                        "I say I had three different requests come in around the same time and each person thought theirs was the priority.",
                        REASONABLE,
                        "This sets up the conflict but leaves out what the requests actually were, which is the detail that makes the prioritization decision evaluable.",
                    ),
                    choice(
                        "prioritization-star-situation-c",
                        "I say I sometimes get pulled in a lot of directions with competing requests.",
                        WEAK,
                        "This generalizes into a recurring pattern rather than describing one specific moment, which is what a STAR situation needs to stay credible.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "prioritization-star-task-a",
                        "I say my task was to decide an actual sequence and communicate it clearly to all three stakeholders, rather than trying to work on all three at once or silently picking one and letting the others assume they were still on track.",
                        STRONG,
                        "This frames the task around both deciding and communicating the sequence, which is exactly what a prioritization question is testing beyond just picking a winner.",
                    ),
                    choice(
                        "prioritization-star-task-b",
                        "I say my task was to figure out which one was truly the most urgent.",
                        REASONABLE,
                        "This captures half the task - the decision - but skips the equally important part of communicating that decision to the people waiting on the other two.",
                    ),
                    choice(
                        "prioritization-star-task-c",
                        "I say my task was to make sure none of the three stakeholders got upset with me.",
                        WEAK,
                        "Optimizing for avoiding upset stakeholders, rather than for the actual impact of each request, is the wrong criterion for a prioritization decision.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "prioritization-star-action-a",
                        "I describe quickly assessing impact and reversibility - the bug was actively costing customers money, the demo had a fixed external date, the migration didn't - then messaging all three stakeholders the same day with the order and a specific time estimate for each.",
                        STRONG,
                        "Using explicit criteria (impact, reversibility, hard external dates) to sequence the work, then communicating it proactively, shows a real, repeatable prioritization method.",
                    ),
                    choice(
                        "prioritization-star-action-b",
                        "I say I picked the bug fix first since it seemed most urgent and let the other two know I'd get to them after.",
                        REASONABLE,
                        "This lands on a reasonable order, but 'seemed most urgent' doesn't show the actual reasoning behind the sequencing decision.",
                    ),
                    choice(
                        "prioritization-star-action-c",
                        "I say I started on whichever request had come in first and told the others I'd get to them eventually.",
                        WEAK,
                        "Sequencing by arrival order rather than by impact or deadline ignores the actual tradeoffs between the three requests, which is what the question is testing.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "prioritization-star-result-a",
                        "I say the bug was fixed within four hours before it affected more customers, the demo feature shipped a day ahead of the client meeting, and the migration was still finished within its window - all three stakeholders knew where they stood the whole time.",
                        STRONG,
                        "Specific outcomes for all three competing requests, plus the fact that everyone stayed informed throughout, makes this a complete and credible result.",
                    ),
                    choice(
                        "prioritization-star-result-b",
                        "I say all three things eventually got done without any major problems.",
                        REASONABLE,
                        "This confirms a good outcome but gives no sense of how close any of the three came to slipping, so the interviewer can't judge how well the tradeoffs were actually managed.",
                    ),
                    choice(
                        "prioritization-star-result-c",
                        "I say it was a stressful week but everything worked out okay.",
                        WEAK,
                        "This is vague reassurance with no measurable outcome for any of the three requests, so it doesn't actually demonstrate the prioritization worked.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "prioritization-star-reflection-a",
                        "I say I learned to prioritize by actual impact and reversibility rather than by who asked loudest or first, and I now message everyone the sequence explicitly instead of assuming they'll be fine waiting.",
                        STRONG,
                        "This names a specific, reusable framework (impact and reversibility) plus a communication habit you've kept using - strong evidence of a lesson that stuck.",
                    ),
                    choice(
                        "prioritization-star-reflection-b",
                        "I say I learned that clear communication matters a lot when juggling multiple priorities.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific criteria you now use to decide the sequence itself.",
                    ),
                    choice(
                        "prioritization-star-reflection-c",
                        "I say I learned that you just have to do your best when things get busy.",
                        WEAK,
                        "This is vague reassurance rather than an actual lesson - it doesn't say what you'd do differently the next time priorities collide.",
                    ),
                ),
            ),
        ),
    )

    private val communicationStar = StarWorkout(
        id = "communication-stakeholder-star",
        commonQuestion = "Tell me about a time you had to communicate complex or technical information to stakeholders with varying levels of expertise.",
        category = COMMUNICATION,
        scenario = "You had to tell a non-technical product manager that a feature already promised to a client " +
            "would need to slip by two weeks because of a database performance issue discovered late in testing.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "communication-star-situation-a",
                        "I describe the exact situation - a feature the product manager had already told a client would ship in two weeks, and a database performance issue I found during final testing that meant it wasn't safe to ship on time.",
                        STRONG,
                        "Naming the specific promise already made to the client and the exact technical issue discovered makes the stakes of the communication challenge concrete.",
                    ),
                    choice(
                        "communication-star-situation-b",
                        "I say I found a technical issue that meant a feature the PM had promised to a client would be late.",
                        REASONABLE,
                        "This states the problem but skips the specific timing and stakes (already promised, found late in testing) that make the communication challenge feel real.",
                    ),
                    choice(
                        "communication-star-situation-c",
                        "I say sometimes technical problems come up that affect what's been promised to clients.",
                        WEAK,
                        "This generalizes into a pattern rather than describing one specific moment, which is what a STAR situation needs to stay credible.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "communication-star-task-a",
                        "I say my task was to explain a technical problem to a non-technical stakeholder in a way that let her actually make a decision, not just tell her the bad news and leave it there.",
                        STRONG,
                        "Framing the task around enabling a decision, not just delivering bad news, is exactly what a communication question is trying to surface.",
                    ),
                    choice(
                        "communication-star-task-b",
                        "I say my task was to let the product manager know as soon as possible that the date would slip.",
                        REASONABLE,
                        "Speed is genuinely important here, but framing the task only around timing skips the harder part - making the explanation actually useful to her.",
                    ),
                    choice(
                        "communication-star-task-c",
                        "I say my task was to try to still hit the original date so the PM wouldn't have to have that conversation with the client.",
                        WEAK,
                        "Prioritizing avoiding the conversation over giving an honest, timely update risks a worse outcome if the date slips anyway with even less warning.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "communication-star-action-a",
                        "I describe explaining the problem in plain terms - the feature would work but could slow down or crash under real client traffic - then giving her two concrete options with different tradeoffs so she could decide with the client context I didn't have.",
                        STRONG,
                        "Translating the technical issue into plain-language stakes and offering decision-ready options is exactly the kind of communication that helps a non-technical stakeholder act.",
                    ),
                    choice(
                        "communication-star-action-b",
                        "I say I explained the technical issue as clearly as I could and told her I needed two more weeks.",
                        REASONABLE,
                        "This communicates the problem honestly, but presenting only one option leaves her without any real choice in how to handle the client.",
                    ),
                    choice(
                        "communication-star-action-c",
                        "I say I sent her a detailed technical writeup of the database issue so she'd understand exactly what happened.",
                        WEAK,
                        "A detailed technical writeup is the wrong format for a non-technical stakeholder who needs to make a decision, not evaluate the engineering in depth.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "communication-star-result-a",
                        "I say she chose the usage-cap option, presented it to the client herself using the framing I gave her, the client accepted it without issue, and she told me afterward that having two real options made the conversation much easier than just hearing \"it's delayed.\"",
                        STRONG,
                        "A specific decision made, a successful client conversation, and direct feedback that the framing helped together make this a complete, credible result.",
                    ),
                    choice(
                        "communication-star-result-b",
                        "I say she was able to tell the client about the delay and the client was understanding about it.",
                        REASONABLE,
                        "This confirms a good outcome but doesn't show what specifically about your communication made that conversation go well.",
                    ),
                    choice(
                        "communication-star-result-c",
                        "I say the feature eventually shipped and nobody seemed too bothered by the delay.",
                        WEAK,
                        "This is vague and doesn't connect back to how the communication itself was handled, which is the actual subject of the story.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "communication-star-reflection-a",
                        "I say I learned that translating a technical problem into decision-ready options, not just a plain-language explanation, is what actually helps a non-technical stakeholder - and I now default to bringing at least two options whenever I deliver bad technical news.",
                        STRONG,
                        "This names a specific, reusable technique (decision-ready options) the experience taught you and confirms you've kept using it - strong evidence of real reflection.",
                    ),
                    choice(
                        "communication-star-reflection-b",
                        "I say I learned it's important to explain technical issues in non-technical terms.",
                        REASONABLE,
                        "This is true but fairly generic - it doesn't capture the more specific insight about offering options rather than just a plain explanation.",
                    ),
                    choice(
                        "communication-star-reflection-c",
                        "I say I learned that communication is really important.",
                        WEAK,
                        "This is too broad to be a real takeaway - it doesn't name anything specific you'd actually do differently next time.",
                    ),
                ),
            ),
        ),
    )

    private val projectImpactStar = StarWorkout(
        id = "project-impact-caching-star",
        commonQuestion = "Tell me about a project you're most proud of and the impact it had.",
        category = PROJECT_IMPACT,
        scenario = "You built a caching layer for a slow internal search tool used by most of the company daily, " +
            "without being asked to - you just noticed how much time people were losing to it.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation part of your answer?",
                listOf(
                    choice(
                        "project-impact-star-situation-a",
                        "I describe the exact problem - an internal search tool that took 8-12 seconds per query, used by roughly 200 employees daily, that nobody had prioritized fixing because it wasn't customer-facing.",
                        STRONG,
                        "Specific numbers (query time, daily users) turn a vague complaint about a slow tool into a concrete, verifiable problem worth solving.",
                    ),
                    choice(
                        "project-impact-star-situation-b",
                        "I say I noticed an internal tool that a lot of people used was really slow.",
                        REASONABLE,
                        "This states the problem but skips the scale (how slow, how many users) that makes the case for why it was worth your time to fix.",
                    ),
                    choice(
                        "project-impact-star-situation-c",
                        "I say internal tools are sometimes slower than they should be.",
                        WEAK,
                        "This generalizes into an observation about tools in general rather than describing one specific problem you noticed and acted on.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "project-impact-star-task-a",
                        "I say my task was to make the case for spending time on something that wasn't customer-facing and wasn't on anyone's roadmap, by showing it was worth the investment before asking for time to build it.",
                        STRONG,
                        "Framing the task around justifying the investment, not just building the fix, shows the kind of judgment interviewers want to see in an unprompted impact story.",
                    ),
                    choice(
                        "project-impact-star-task-b",
                        "I say my task was to see if I could speed up the search tool in my spare time.",
                        REASONABLE,
                        "This is a reasonable starting point, but framing it purely as a spare-time experiment skips the harder task of making it a legitimate, resourced piece of work.",
                    ),
                    choice(
                        "project-impact-star-task-c",
                        "I say my task was to fix the search tool since it was bothering me personally.",
                        WEAK,
                        "Framing the motivation around personal annoyance rather than the tool's actual company-wide cost undersells the judgment behind picking this problem.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "project-impact-star-action-a",
                        "I describe timing 50 sample queries to quantify the delay, proposing a caching layer for the most common queries in a two-paragraph doc with an estimated engineering cost, getting a half-day approved to prototype it, then building and shipping it after the prototype showed a clear speedup.",
                        STRONG,
                        "Quantifying the problem, proposing a scoped solution with a cost estimate, and validating with a small prototype before committing fully is a concrete, credible method for driving unprompted impact.",
                    ),
                    choice(
                        "project-impact-star-action-b",
                        "I say I built a prototype caching layer in my spare time and showed it to my manager once it was working.",
                        REASONABLE,
                        "This shows initiative, but building the whole prototype before involving anyone else means the investment happened without any check-in on whether it was the right use of your time.",
                    ),
                    choice(
                        "project-impact-star-action-c",
                        "I say I just started building the caching layer since I was confident it would help.",
                        WEAK,
                        "Committing significant unscoped time to a self-directed project without quantifying the problem or checking in with anyone is a risky way to spend engineering effort, confidence aside.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "project-impact-star-result-a",
                        "I say average query time dropped from 10 seconds to under 1 second, and a survey the ops team ran afterward estimated it saved the company roughly 40 hours of employee time per week.",
                        STRONG,
                        "A precise before-and-after metric plus an independently estimated company-wide time savings makes the impact concrete and hard to dispute.",
                    ),
                    choice(
                        "project-impact-star-result-b",
                        "I say the search tool got noticeably faster and people seemed happy about it.",
                        REASONABLE,
                        "This confirms a positive outcome but gives no measurable scale, so the interviewer can't judge how significant the actual impact was.",
                    ),
                    choice(
                        "project-impact-star-result-c",
                        "I say people mentioned the tool felt better after the change.",
                        WEAK,
                        "Anecdotal comments with no measurement at all is a weak way to close out a story whose whole premise was unprompted, self-justified impact.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "project-impact-star-reflection-a",
                        "I say I learned that quantifying an unglamorous problem before proposing to fix it is what gets time approved for it, and I now default to a quick cost estimate before asking to work on anything off the official roadmap.",
                        STRONG,
                        "This names a specific, reusable habit (quantify before proposing) the experience taught you and confirms you still apply it, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "project-impact-star-reflection-b",
                        "I say I learned that even small internal improvements can have a real impact.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific technique (quantifying and proposing) this experience actually taught you.",
                    ),
                    choice(
                        "project-impact-star-reflection-c",
                        "I say I learned that it's good to fix things that bother you when you get the chance.",
                        WEAK,
                        "This centers on personal annoyance again rather than naming any actual lesson about how you now approach unprompted work.",
                    ),
                ),
            ),
        ),
    )

    // ---------------------------------------------------------------- DEVELOPING tier (harder judgment calls)
    //
    // Same categories, a different real, commonly-asked interview question,
    // and choices where the strong/reasonable/weak distinction is more
    // subtle - the DEVELOPING analog of ProblemWorkouts' harder tiers in
    // Practice. Authored for three categories as the quality reference;
    // remaining categories and tiers land in a follow-up pass.

    private val leadershipDevelopingStar = StarWorkout(
        id = "leadership-motivation-developing-star",
        category = LEADERSHIP,
        difficulty = DEVELOPING,
        commonQuestion = "Describe a time you had to motivate a team during a difficult period.",
        scenario = "Your team had just lost two members to attrition mid-quarter, and the remaining engineers " +
            "were visibly discouraged, with velocity dropping noticeably in the two weeks since.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "leadership-dev-situation-a",
                        "I describe the specific timing (two members leaving mid-quarter), the concrete signal of the problem (velocity dropping over two specific weeks), and why it mattered (a commitment to a stakeholder was now at risk).",
                        STRONG,
                        "Naming the specific trigger, a measurable signal, and the real stake gives the interviewer a situation they can actually evaluate, not just 'the team was down.'",
                    ),
                    choice(
                        "leadership-dev-situation-b",
                        "I say the team was short-staffed and morale was low.",
                        REASONABLE,
                        "This is directionally right but skips the concrete numbers and timing that make a motivation story credible rather than a generic complaint.",
                    ),
                    choice(
                        "leadership-dev-situation-c",
                        "I say team motivation is something I always pay close attention to.",
                        WEAK,
                        "This turns the situation into a personal trait statement instead of one specific moment, which is exactly what a STAR situation should avoid.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "leadership-dev-task-a",
                        "I say I decided my task was to rebuild a sense of forward progress quickly, since dwelling on the loss for too long would compound the slowdown - not to replace the two people right away.",
                        STRONG,
                        "This shows a deliberate, prioritized read of the actual problem (momentum, not headcount) rather than jumping to the most obvious but slower fix.",
                    ),
                    choice(
                        "leadership-dev-task-b",
                        "I say my task was to keep the team's spirits up until we could hire replacements.",
                        REASONABLE,
                        "This is a fair goal, but framing it as waiting on hiring puts the fix outside your control, when the story should show what you did regardless of headcount.",
                    ),
                    choice(
                        "leadership-dev-task-c",
                        "I say my task was to make sure nobody else quit.",
                        WEAK,
                        "This reframes the task around avoiding a worst case rather than actually improving the situation, and it's hard to demonstrate you achieved a negative.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "leadership-dev-action-a",
                        "I describe re-scoping the quarter's plan down to a smaller, clearly achievable set of wins within a week, then publicly closing out the first one to give the team a visible result to point to.",
                        STRONG,
                        "A concrete, sequenced action - rescope, then create an early visible win - shows a specific method for rebuilding momentum, not just 'I talked to them.'",
                    ),
                    choice(
                        "leadership-dev-action-b",
                        "I say I held a team meeting to acknowledge the loss and encourage everyone to stay positive.",
                        REASONABLE,
                        "Acknowledging the situation is a reasonable first step, but on its own it's a conversation, not a plan that actually changes the team's trajectory.",
                    ),
                    choice(
                        "leadership-dev-action-c",
                        "I say I took on extra work myself so the team's output wouldn't drop as much.",
                        WEAK,
                        "Absorbing the gap personally might help in the short term, but it doesn't address the team's morale or the underlying instability, and it isn't sustainable.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "leadership-dev-result-a",
                        "I say the team shipped the rescoped milestone on the new date, and in a retro afterward two engineers specifically mentioned that seeing a clear win early made the rest of the quarter feel possible again.",
                        STRONG,
                        "A concrete outcome (shipped on the new date) plus direct evidence the morale fix worked (specific feedback in retro) makes this result both measurable and credible.",
                    ),
                    choice(
                        "leadership-dev-result-b",
                        "I say the team's mood noticeably improved over the following weeks.",
                        REASONABLE,
                        "This is a real outcome, but 'noticeably improved mood' without a specific measure or quote is harder for an interviewer to verify or picture.",
                    ),
                    choice(
                        "leadership-dev-result-c",
                        "I say everything worked out and the quarter was fine in the end.",
                        WEAK,
                        "This is a vague, feel-good wrap-up with no specific evidence the motivation effort itself made a difference.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "leadership-dev-reflection-a",
                        "I say I learned that in a demotivated team, a small guaranteed win early matters more than a big plan, and I now deliberately break work into an early visible milestone whenever a team is under strain.",
                        STRONG,
                        "This names a specific, transferable principle (early guaranteed win over big plan) and shows you've applied it since - real evidence of reflection.",
                    ),
                    choice(
                        "leadership-dev-reflection-b",
                        "I say I learned that keeping communication open during hard times is important.",
                        REASONABLE,
                        "True but generic - this could follow almost any team story and doesn't capture the specific lesson this situation taught you.",
                    ),
                    choice(
                        "leadership-dev-reflection-c",
                        "I say I'd probably handle it about the same way again.",
                        WEAK,
                        "This doesn't name a lesson at all - it just confirms you were satisfied, which isn't what Reflection is asking for.",
                    ),
                ),
            ),
        ),
    )

    private val conflictDevelopingStar = StarWorkout(
        id = "conflict-manager-disagreement-developing-star",
        category = CONFLICT,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you disagreed with your manager.",
        scenario = "Your manager wanted to cut a planned code-review step to hit a launch date, and you believed " +
            "skipping it created real risk given the size of the change going out.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "conflict-dev-situation-a",
                        "I describe the specific proposal (skip review to hit the date), the specific change at stake (a large migration touching a core service), and why that combination worried me.",
                        STRONG,
                        "Naming the specific proposal and what made it risky in this case gives the interviewer a real situation to evaluate, not a generic 'my manager and I disagreed.'",
                    ),
                    choice(
                        "conflict-dev-situation-b",
                        "I say my manager wanted to move faster than I was comfortable with.",
                        REASONABLE,
                        "This states the tension but leaves out what was actually being proposed and why it concerned you, which is what makes the disagreement concrete.",
                    ),
                    choice(
                        "conflict-dev-situation-c",
                        "I say managers and engineers often see risk differently.",
                        WEAK,
                        "This generalizes into an abstract dynamic rather than describing one specific moment you can walk through.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "conflict-dev-task-a",
                        "I say my task was to raise the risk clearly enough that my manager could make an informed call, even if that call ended up being to proceed anyway.",
                        STRONG,
                        "This frames the task around ensuring an informed decision, not around 'winning' the disagreement - exactly the framing that reads well with a manager-level conflict.",
                    ),
                    choice(
                        "conflict-dev-task-b",
                        "I say my task was to convince my manager to keep the review step.",
                        REASONABLE,
                        "This is honest, but framing the task purely as persuasion misses that with a manager, being heard and creating an informed decision matters as much as the outcome.",
                    ),
                    choice(
                        "conflict-dev-task-c",
                        "I say my task was to decide whether to go over my manager's head.",
                        WEAK,
                        "Jumping to escalation as the task, before even attempting a direct conversation, skips the step most interviewers are actually listening for.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "conflict-dev-action-a",
                        "I described the specific failure mode I was worried about, offered a faster partial review that still covered the highest-risk files, and asked my manager to make the final call once she had that option in front of her.",
                        STRONG,
                        "Offering a concrete middle option, not just raising the concern, shows you tried to solve the actual problem rather than just registering disagreement.",
                    ),
                    choice(
                        "conflict-dev-action-b",
                        "I explained my concerns clearly and asked her to reconsider.",
                        REASONABLE,
                        "This is a reasonable and respectful way to raise the issue, but it doesn't offer any alternative path, leaving your manager only a binary choice.",
                    ),
                    choice(
                        "conflict-dev-action-c",
                        "I did the full review anyway without telling her, to make sure it happened.",
                        WEAK,
                        "Quietly overriding your manager's decision, even with good intentions, undermines trust and isn't really resolving the disagreement - it's avoiding it.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "conflict-dev-result-a",
                        "I say my manager agreed to the faster partial review, it caught a real bug in one of the highest-risk files before launch, and she told me afterward she appreciated having the option laid out instead of just a flat objection.",
                        STRONG,
                        "A concrete outcome (caught a real bug) plus direct positive feedback from the manager makes this result specific and verifiable.",
                    ),
                    choice(
                        "conflict-dev-result-b",
                        "I say she ultimately decided to skip the review, but the launch went fine.",
                        REASONABLE,
                        "This is an honest outcome, but 'went fine' doesn't tell the interviewer whether your original concern turned out to be justified or not.",
                    ),
                    choice(
                        "conflict-dev-result-c",
                        "I say we agreed to disagree and moved on.",
                        WEAK,
                        "This suggests the disagreement was left unresolved rather than reaching an actual decision, which reads as unfinished rather than resolved.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "conflict-dev-reflection-a",
                        "I say I learned that offering a concrete middle option changes a disagreement with a manager from a pushback into a decision they can act on, and I've used that approach with every manager-level disagreement since.",
                        STRONG,
                        "This names a specific, reusable technique and confirms it's become a consistent practice - strong evidence of genuine reflection.",
                    ),
                    choice(
                        "conflict-dev-reflection-b",
                        "I say I learned it's okay to disagree with your manager as long as you're respectful.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific insight (offering an alternative, not just objecting) this situation actually taught.",
                    ),
                    choice(
                        "conflict-dev-reflection-c",
                        "I say I learned that my manager usually knows best.",
                        WEAK,
                        "This undercuts the whole premise of the story - if the lesson is to defer to your manager, it's unclear why you're using this as an example of raising a disagreement at all.",
                    ),
                ),
            ),
        ),
    )

    private val failureLearningDevelopingStar = StarWorkout(
        id = "failure-learning-feedback-developing-star",
        category = FAILURE_LEARNING,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you received difficult feedback and how you responded.",
        scenario = "In a performance review, your manager told you that your code reviews on other people's PRs " +
            "were technically strong but often came across as blunt, and that a couple of teammates had mentioned dreading review from you.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "failure-dev-situation-a",
                        "I describe the specific feedback (blunt review comments, named by more than one teammate) and how it landed - it stung because I saw myself as being direct and efficient, not harsh.",
                        STRONG,
                        "Naming the specific feedback and your honest first reaction to it makes the situation feel real and sets up genuine growth, rather than a sanitized version of events.",
                    ),
                    choice(
                        "failure-dev-situation-b",
                        "I say my manager told me I needed to work on how I communicate.",
                        REASONABLE,
                        "This states that feedback occurred but loses the specific, uncomfortable detail (blunt reviews, named by teammates) that makes the story concrete and honest.",
                    ),
                    choice(
                        "failure-dev-situation-c",
                        "I say I'm always open to feedback and welcome criticism.",
                        WEAK,
                        "This turns the situation into a general personality claim instead of describing one specific piece of feedback and how it actually felt to receive it.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "failure-dev-task-a",
                        "I say my task was to figure out specifically what 'blunt' meant in practice - not just resolve to 'be nicer' - so I could actually change the pattern rather than just feel bad about it.",
                        STRONG,
                        "This frames the task around getting specific, actionable understanding rather than a vague resolution, which is what turns feedback into real change.",
                    ),
                    choice(
                        "failure-dev-task-b",
                        "I say my task was to be more careful with my wording in reviews going forward.",
                        REASONABLE,
                        "This is a reasonable intention, but it's vague enough that it's hard to tell whether it actually changed - it could describe almost any feedback response.",
                    ),
                    choice(
                        "failure-dev-task-c",
                        "I say my task was to prove the feedback wasn't really fair.",
                        WEAK,
                        "Treating the task as defending yourself rather than understanding the feedback undermines the whole premise of a growth story.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "failure-dev-action-a",
                        "I asked my manager for one or two specific examples of comments that landed badly, noticed they were all terse one-line 'this is wrong, fix it' style, and started rewriting my review comments to name the specific issue and suggest a fix in the same breath.",
                        STRONG,
                        "Getting concrete examples and identifying an actual pattern (terse comments) shows real investigation, and rewriting your approach based on that pattern is a specific, repeatable action.",
                    ),
                    choice(
                        "failure-dev-action-b",
                        "I made an effort to add more context and be kinder in my review comments.",
                        REASONABLE,
                        "This is a genuine effort, but without a specific method, like identifying the actual pattern in your old comments, it's hard to know it produced a real, lasting change.",
                    ),
                    choice(
                        "failure-dev-action-c",
                        "I started adding a compliment before every piece of critical feedback.",
                        WEAK,
                        "This treats the symptom (tone) without addressing the substance (unclear, unexplained criticism), and can come across as formulaic rather than genuinely clearer.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "failure-dev-result-a",
                        "I say two review cycles later, one of the teammates who'd raised the concern told me directly that my comments felt much easier to act on, and my manager noted the change in my next check-in without me bringing it up.",
                        STRONG,
                        "Unprompted positive feedback from both the original teammate and your manager is strong, specific evidence the change actually worked, not just that you tried.",
                    ),
                    choice(
                        "failure-dev-result-b",
                        "I say the feedback seemed to improve over the following months.",
                        REASONABLE,
                        "This is a plausible outcome, but 'seemed to improve' without a specific example or source is hard for an interviewer to weigh.",
                    ),
                    choice(
                        "failure-dev-result-c",
                        "I say I haven't heard any complaints since.",
                        WEAK,
                        "Absence of complaints is weak evidence on its own - it could just as easily mean nobody brought it up again, not that anything actually changed.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "failure-dev-reflection-a",
                        "I say I learned that 'blunt' feedback about communication almost always has a specific, fixable pattern behind it, and I now ask for one concrete example whenever I get feedback that's hard to act on.",
                        STRONG,
                        "This names a precise, reusable habit (always ask for a concrete example) that came directly from this experience - strong, specific reflection.",
                    ),
                    choice(
                        "failure-dev-reflection-b",
                        "I say I learned that how you say something matters as much as what you say.",
                        REASONABLE,
                        "This is a true and relevant lesson, but it's generic enough to apply to almost any communication feedback story.",
                    ),
                    choice(
                        "failure-dev-reflection-c",
                        "I say feedback like that is just part of working with people.",
                        WEAK,
                        "This treats the feedback as an unavoidable fact of life rather than naming anything you actually learned or changed.",
                    ),
                ),
            ),
        ),
    )

    private val ownershipDevelopingStar = StarWorkout(
        id = "ownership-going-above-developing-star",
        category = OWNERSHIP,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you went above and beyond your job responsibilities.",
        scenario = "You noticed the on-call runbook your team relied on during incidents was several versions out of " +
            "date, with steps that no longer matched the current infrastructure - something nobody had explicitly " +
            "asked you to fix.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "ownership-dev-situation-a",
                        "I describe following the runbook during an on-call shift, hitting a step that referenced a server decommissioned months earlier, losing 20 minutes before realizing the doc itself was the problem.",
                        STRONG,
                        "Naming the specific failed step and the concrete cost (20 minutes during an active incident) makes the situation verifiable and shows why it mattered enough to act on.",
                    ),
                    choice(
                        "ownership-dev-situation-b",
                        "I say the on-call runbook was outdated and confusing to use.",
                        REASONABLE,
                        "This states the problem but skips the specific incident and cost that actually motivated you to act, which is what makes the story concrete rather than a general complaint.",
                    ),
                    choice(
                        "ownership-dev-situation-c",
                        "I say documentation at most companies tends to fall out of date over time.",
                        WEAK,
                        "This generalizes into an observation about documentation everywhere rather than describing the one specific moment that prompted you to act.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "ownership-dev-task-a",
                        "I say I decided the task was to audit and fix the entire runbook against current infrastructure, not just patch the one step that had failed, since a single fix wouldn't stop the next on-call engineer from hitting a different stale step.",
                        STRONG,
                        "Choosing the broader, more thorough fix over the quicker patch shows the kind of judgment call that separates genuinely going above and beyond from doing the bare minimum.",
                    ),
                    choice(
                        "ownership-dev-task-b",
                        "I say my task was to fix the specific step that had failed during my shift.",
                        REASONABLE,
                        "This addresses the immediate problem, but stopping at the one broken step is a narrower scope than the situation actually called for.",
                    ),
                    choice(
                        "ownership-dev-task-c",
                        "I say my task was to let whoever normally owned the doc know it needed updating.",
                        WEAK,
                        "Handing the problem to an undefined owner, when no one clearly owned the doc in the first place, is unlikely to actually get it fixed.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "ownership-dev-action-a",
                        "I describe blocking out time across two days when I wasn't on call, walking every single step against the actual current infrastructure, testing risky commands in a sandbox first, and getting two senior on-call engineers to review before merging.",
                        STRONG,
                        "A methodical, verified process like this - testing before merging, getting review from the people who'd actually rely on it - shows real rigor behind the initiative, not just good intentions.",
                    ),
                    choice(
                        "ownership-dev-action-b",
                        "I went through the runbook and updated the steps that looked wrong based on what I remembered.",
                        REASONABLE,
                        "This is a genuine effort, but relying on memory rather than verifying each step against the real infrastructure risks leaving other stale steps in place.",
                    ),
                    choice(
                        "ownership-dev-action-c",
                        "I rewrote the whole runbook from scratch in the structure I personally found clearest, without checking with anyone who used it regularly.",
                        WEAK,
                        "Redesigning the document around your own preferences, without input from the people who actually rely on it during incidents, risks solving the wrong problem.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "ownership-dev-result-a",
                        "I say another engineer used the updated runbook successfully during a later incident and told me afterward it saved real time, and two other teams asked to use it as the template for their own runbooks.",
                        STRONG,
                        "A concrete later use case plus adoption by other teams makes the impact of the fix measurable and credible, not just a one-time cleanup.",
                    ),
                    choice(
                        "ownership-dev-result-b",
                        "I say the runbook was more accurate afterward and hasn't caused problems since.",
                        REASONABLE,
                        "This is a fair outcome, but without a specific example of it actually helping someone, the impact is harder for an interviewer to judge.",
                    ),
                    choice(
                        "ownership-dev-result-c",
                        "I say people on the team probably appreciate having a more current doc now.",
                        WEAK,
                        "This is speculation about how people feel rather than a specific, observed outcome, which makes the result hard to evaluate.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "ownership-dev-reflection-a",
                        "I say I learned that fixing the underlying document, not just the one step that broke, is what actually prevents the next person from hitting a different stale step, and I now treat any doc gap found mid-incident as worth a full audit rather than a quick patch.",
                        STRONG,
                        "This names a specific, transferable principle - fix the whole thing, not just the symptom - and shows it's become a standing practice, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "ownership-dev-reflection-b",
                        "I say I learned that it's good to keep documentation up to date.",
                        REASONABLE,
                        "This is true but generic enough to apply to almost any documentation story, without capturing the specific lesson about scope this situation taught you.",
                    ),
                    choice(
                        "ownership-dev-reflection-c",
                        "I say I learned that things like this come up sometimes on any team.",
                        WEAK,
                        "This normalizes the gap rather than naming anything you'd actually do differently, which misses the point of the stage.",
                    ),
                ),
            ),
        ),
    )

    private val teamworkDevelopingStar = StarWorkout(
        id = "teamwork-working-style-developing-star",
        category = TEAMWORK,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you had to work with someone whose working style was very different from yours.",
        scenario = "You were paired with a teammate who preferred to think through problems alone for days before " +
            "sharing anything, while you were used to thinking out loud and iterating quickly - and the mismatch " +
            "started slowing down a project with a real deadline three weeks out.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "teamwork-dev-situation-a",
                        "I describe two weeks into the project, my teammate hadn't shared any progress yet, I was used to daily syncs, and the gap between our rhythms was starting to create real tension as the three-week deadline got closer.",
                        STRONG,
                        "Naming the specific timeline (two weeks in, three weeks left) and the concrete gap in rhythms makes the mismatch feel real and sets up a genuine tension to resolve.",
                    ),
                    choice(
                        "teamwork-dev-situation-b",
                        "I say a teammate had a very different working style that made collaborating harder.",
                        REASONABLE,
                        "This names the mismatch but skips the specific detail (what the different styles actually were, the deadline pressure) that makes the tension concrete.",
                    ),
                    choice(
                        "teamwork-dev-situation-c",
                        "I say people naturally have different working styles and that's just something you deal with.",
                        WEAK,
                        "This generalizes into an observation about people in general rather than describing one specific situation you had to navigate.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "teamwork-dev-task-a",
                        "I say my task was to find a way of collaborating that actually respected how my teammate worked best, rather than pushing them into my own preferred rhythm just because it was more comfortable for me.",
                        STRONG,
                        "Recognizing that your own preferred style isn't automatically the correct one, and framing the task around finding a shared approach, is exactly the harder judgment call this kind of question is testing.",
                    ),
                    choice(
                        "teamwork-dev-task-b",
                        "I say my task was to get my teammate to share progress more frequently.",
                        REASONABLE,
                        "This is a fair immediate goal, but framing the task only around changing the teammate's behavior misses that the fix likely needed to come from both sides.",
                    ),
                    choice(
                        "teamwork-dev-task-c",
                        "I say my task was to work around my teammate by picking up more of the work myself.",
                        WEAK,
                        "Quietly absorbing more work instead of addressing the mismatch directly avoids the actual collaboration problem rather than solving it.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "teamwork-dev-action-a",
                        "I describe proposing a compromise - my teammate keeps uninterrupted blocks of alone time but commits to a short written update every two days instead of daily verbal syncs - then trying it for a week and adjusting the cadence together based on how it went.",
                        STRONG,
                        "Proposing a specific, testable compromise that preserves what actually worked for your teammate, then iterating on it together, shows real collaborative problem-solving rather than a one-sided fix.",
                    ),
                    choice(
                        "teamwork-dev-action-b",
                        "I asked my teammate directly to start checking in more often.",
                        REASONABLE,
                        "This is a direct and honest ask, but it doesn't account for why the teammate worked the way they did, which is what a lasting compromise needed to address.",
                    ),
                    choice(
                        "teamwork-dev-action-c",
                        "I started scheduling recurring meetings on the calendar without asking first, assuming my teammate would adapt once they had to attend.",
                        WEAK,
                        "Forcing a change without discussing it first risks resentment and doesn't actually address why the working styles clashed in the first place.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "teamwork-dev-result-a",
                        "I say the project shipped on time, my teammate later told me the written cadence actually helped them organize their thinking better than verbal syncs would have, and we kept the same structure on our next project together.",
                        STRONG,
                        "A completed project, direct positive feedback from the teammate, and the habit carrying into future work together make this a specific, credible, well-rounded result.",
                    ),
                    choice(
                        "teamwork-dev-result-b",
                        "I say the project got done and things went okay between the two of us.",
                        REASONABLE,
                        "This confirms a fine outcome but gives no specific evidence that the compromise itself was what made it work.",
                    ),
                    choice(
                        "teamwork-dev-result-c",
                        "I say eventually my teammate started communicating more.",
                        WEAK,
                        "This is vague and doesn't connect back to the specific compromise you proposed, so it doesn't show your action caused the improvement.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "teamwork-dev-reflection-a",
                        "I say I learned that a good collaboration process isn't the one I personally prefer, it's the one that fits the specific people involved, and I now ask new collaborators how they like to work early on instead of assuming my own rhythm is the default.",
                        STRONG,
                        "This names a specific, transferable principle - the right process depends on the people, not your own default - and shows you've made it a standing habit, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "teamwork-dev-reflection-b",
                        "I say I learned it's important to be flexible when working with people who are different from you.",
                        REASONABLE,
                        "This is true but fairly generic - it doesn't capture the specific insight about proactively asking how someone likes to work.",
                    ),
                    choice(
                        "teamwork-dev-reflection-c",
                        "I say I learned that everyone just works differently.",
                        WEAK,
                        "This is an observation about people in general rather than a lesson about what you'd actually do differently next time.",
                    ),
                ),
            ),
        ),
    )

    private val ambiguityDevelopingStar = StarWorkout(
        id = "ambiguity-incomplete-information-developing-star",
        category = AMBIGUITY,
        difficulty = DEVELOPING,
        commonQuestion = "Describe a time you had to make a decision with incomplete information.",
        scenario = "Fifteen minutes after launching a new payment feature, you saw the failure rate jump from a 0.5% " +
            "baseline to 4%, with no confirmation yet of whether the cause was your change or an unrelated issue " +
            "with the third-party payment processor.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "ambiguity-dev-situation-a",
                        "I describe the exact numbers - failure rate jumping from 0.5% to 4% within 15 minutes of launch - and that the processor's own status page showed no reported outage, leaving genuine uncertainty about the cause with real revenue at stake.",
                        STRONG,
                        "Specific before-and-after numbers plus the detail that the processor showed no outage makes the uncertainty concrete and verifiable, not just a vague 'something seemed off.'",
                    ),
                    choice(
                        "ambiguity-dev-situation-b",
                        "I say a feature I launched showed a spike in errors and it wasn't immediately clear why.",
                        REASONABLE,
                        "This states the problem but skips the specific numbers and the processor detail that make the ambiguity feel genuinely hard to resolve.",
                    ),
                    choice(
                        "ambiguity-dev-situation-c",
                        "I say systems sometimes have issues right after you launch something new.",
                        WEAK,
                        "This generalizes into a pattern about launches in general rather than describing the one specific, high-stakes moment you had to navigate.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "ambiguity-dev-task-a",
                        "I say my task was to decide with the information available in minutes, not to wait for certainty, since every extra minute of delay meant more customers hitting failed transactions either way.",
                        STRONG,
                        "Framing the task around deciding under a time constraint, and recognizing that waiting itself has a cost, is exactly the harder judgment an ambiguity-under-pressure question is testing.",
                    ),
                    choice(
                        "ambiguity-dev-task-b",
                        "I say my task was to figure out whether the spike was actually caused by my change.",
                        REASONABLE,
                        "This is a fair goal, but it treats reaching certainty as the task, when the situation actually called for acting well before certainty was possible.",
                    ),
                    choice(
                        "ambiguity-dev-task-c",
                        "I say my task was to make sure nobody would blame my team if it turned out not to be our fault.",
                        WEAK,
                        "Framing the task around protecting your team's reputation, rather than resolving the customer-facing failure, misses what the situation actually called for.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "ambiguity-dev-action-a",
                        "I describe pulling the error logs immediately and noticing the failures were concentrated in one specific payment path my change had touched, not spread evenly the way a processor-wide outage would be, and rolling back within eight minutes based on that partial signal rather than waiting for the processor to confirm anything.",
                        STRONG,
                        "Acting on a specific, localized signal rather than waiting for full external confirmation shows real judgment under ambiguity - using the best available evidence instead of demanding certainty.",
                    ),
                    choice(
                        "ambiguity-dev-action-b",
                        "I watched the error rate for a few more minutes to see whether it kept climbing before deciding what to do.",
                        REASONABLE,
                        "This is a reasonable instinct to gather more signal, but a few more minutes of climbing failures at this scale has a real cost that a faster partial-evidence decision could have avoided.",
                    ),
                    choice(
                        "ambiguity-dev-action-c",
                        "I waited for the payment processor to confirm an outage on their status page before doing anything, since it might not have been caused by my change.",
                        WEAK,
                        "Waiting for external confirmation before acting, when customers were actively experiencing failures, prioritizes certainty over the actual urgency of the situation.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "ambiguity-dev-result-a",
                        "I say the error rate dropped back to baseline within two minutes of the rollback, confirming it was the change, the root cause turned out to be an edge case in new retry logic, and the feature relaunched safely two days later once fixed.",
                        STRONG,
                        "A fast, measurable confirmation that the decision was correct, plus a concrete root cause and safe relaunch, makes this a complete and credible result.",
                    ),
                    choice(
                        "ambiguity-dev-result-b",
                        "I say the rollback happened and the error rate went back down not long after.",
                        REASONABLE,
                        "This confirms a good outcome but gives no specific timing or confirmation that the rollback itself was the cause, which weakens the case that the decision was correct.",
                    ),
                    choice(
                        "ambiguity-dev-result-c",
                        "I say it eventually got sorted out and payments were fine again.",
                        WEAK,
                        "This is vague and doesn't confirm whether your specific decision to roll back was actually validated, which is the part of the story worth proving.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "ambiguity-dev-reflection-a",
                        "I say I learned that a partial, localized signal is often enough to act on during an active incident, since waiting for full certainty is itself a decision with a real cost, and I now default to acting on a strong partial signal rather than waiting to be sure.",
                        STRONG,
                        "This names a specific, transferable principle - waiting for certainty has its own cost - and confirms it's become a default practice, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "ambiguity-dev-reflection-b",
                        "I say I learned it's better to act quickly during an incident rather than wait too long.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific reasoning about localized versus company-wide signals this situation taught you.",
                    ),
                    choice(
                        "ambiguity-dev-reflection-c",
                        "I say I learned that things like this can happen whenever you launch something new.",
                        WEAK,
                        "This is an observation about launches in general rather than a lesson about how you'd actually decide differently next time.",
                    ),
                ),
            ),
        ),
    )

    private val prioritizationDevelopingStar = StarWorkout(
        id = "prioritization-overloaded-developing-star",
        category = PRIORITIZATION,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you had too much on your plate and how you handled it.",
        scenario = "In the same week, you were the sole engineer finishing a feature due in seven days, on the hook " +
            "to review a teammate's PR that was blocking their own work, and expected to prep a technical deep-dive " +
            "for a director-level meeting in four days - with none of the three requesters aware of the others.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "prioritization-dev-situation-a",
                        "I describe all three commitments landing the same Monday, each person assuming I had the capacity since they had no idea about the other two, and me first realizing the full overlap while checking my calendar Tuesday morning.",
                        STRONG,
                        "Naming the specific timing and the fact that nobody knew about the overlap makes the situation concrete and explains exactly why the conflict wasn't caught earlier.",
                    ),
                    choice(
                        "prioritization-dev-situation-b",
                        "I say I had several big things due around the same time and it felt overwhelming.",
                        REASONABLE,
                        "This states the problem but skips the specific commitments and why nobody had visibility into the overlap, which is what makes the situation credible.",
                    ),
                    choice(
                        "prioritization-dev-situation-c",
                        "I say I'm often busy with a lot of different things at once.",
                        WEAK,
                        "This generalizes into a recurring trait rather than describing the one specific week and set of overlapping commitments.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "prioritization-dev-task-a",
                        "I say my task was to figure out which commitments could genuinely flex and renegotiate those, rather than trying to grind through all three at full quality, which would likely have meant doing all three poorly.",
                        STRONG,
                        "Recognizing that renegotiating scope is often the right call, rather than just working harder, is exactly the harder judgment this kind of prioritization question is testing.",
                    ),
                    choice(
                        "prioritization-dev-task-b",
                        "I say my task was to work extra hours to get all three done as originally planned.",
                        REASONABLE,
                        "This is a common instinct, but treating 'work harder' as the task risks lower quality across all three rather than addressing the actual conflict in commitments.",
                    ),
                    choice(
                        "prioritization-dev-task-c",
                        "I say my task was to make sure the director-level meeting went well since that felt like the most visible one.",
                        WEAK,
                        "Optimizing for visibility rather than for the actual impact and urgency of each commitment is the wrong criterion for deciding what to prioritize.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "prioritization-dev-action-a",
                        "I describe messaging my teammate that the review would take one extra day unless truly blocking, checking with the meeting organizer to confirm the deep-dive could be a working draft rather than a polished deck, and being upfront with all three people about the tradeoff instead of silently deprioritizing anything.",
                        STRONG,
                        "Proactively renegotiating scope with each person and being transparent about the tradeoff shows a real, repeatable method for handling overload, not just quietly hoping it works out.",
                    ),
                    choice(
                        "prioritization-dev-action-b",
                        "I prioritized the feature deadline first, then handled the review and the deep-dive prep back to back afterward, working late two nights to fit everything in.",
                        REASONABLE,
                        "This gets everything done, but working extra hours to avoid renegotiating anything doesn't show the same judgment as proactively adjusting scope with the people involved.",
                    ),
                    choice(
                        "prioritization-dev-action-c",
                        "I focused mainly on the director meeting since it felt the most visible, and let the PR review and the feature date slip without telling anyone in advance.",
                        WEAK,
                        "Letting deadlines slip silently, without giving the affected people any advance notice, is exactly the outcome good prioritization is supposed to prevent.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "prioritization-dev-result-a",
                        "I say the feature shipped on the original date, the review finished one day later than hoped but my teammate said the heads-up meant they weren't blocked, and the director said the working-draft deep-dive made the direction clear even without final polish.",
                        STRONG,
                        "Specific outcomes for all three commitments, including direct confirmation from two of the three people that the renegotiation worked for them, makes this a complete and credible result.",
                    ),
                    choice(
                        "prioritization-dev-result-b",
                        "I say everything got finished by the end of the week without anyone escalating.",
                        REASONABLE,
                        "This confirms a good outcome but gives no sense of how each individual commitment landed, so the interviewer can't judge how well the tradeoffs were actually managed.",
                    ),
                    choice(
                        "prioritization-dev-result-c",
                        "I say it was a rough week but I got through it.",
                        WEAK,
                        "This is vague reassurance with no measurable outcome for any of the three commitments, so it doesn't actually demonstrate the prioritization worked.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "prioritization-dev-reflection-a",
                        "I say I learned that renegotiating scope openly, rather than silently trying to do everything at full quality, protects both my output and other people's trust, and I now flag a capacity conflict the moment two commitments land in the same week instead of waiting until it becomes a crisis.",
                        STRONG,
                        "This names a specific, reusable habit - flag conflicts early, renegotiate openly - and shows it's become a standing practice, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "prioritization-dev-reflection-b",
                        "I say I learned it's important to communicate early when I'm overloaded.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific renegotiation approach this situation actually taught you.",
                    ),
                    choice(
                        "prioritization-dev-reflection-c",
                        "I say I learned that you can get through busy weeks if you push hard enough.",
                        WEAK,
                        "This reinforces working harder as the solution rather than naming any actual change to how you'd handle overlapping commitments next time.",
                    ),
                ),
            ),
        ),
    )

    private val communicationDevelopingStar = StarWorkout(
        id = "communication-persuasion-developing-star",
        category = COMMUNICATION,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you had to persuade someone who disagreed with you.",
        scenario = "A product manager was firmly against adding a confirmation step before a destructive bulk-delete " +
            "action, worried it would slow down power users, while you believed the missing confirmation was a " +
            "serious data-loss risk after seeing a near-miss in a support ticket.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "communication-dev-situation-a",
                        "I describe the PM having already pushed back on the confirmation step twice in planning over power-user friction, and me then seeing a support ticket where a user nearly deleted 200 records from a stray click, caught only by an unrelated bug.",
                        STRONG,
                        "Naming the specific prior pushback and the concrete near-miss gives the interviewer real stakes and context for why persuasion was actually needed here.",
                    ),
                    choice(
                        "communication-dev-situation-b",
                        "I say a product manager disagreed with adding a safety step I thought was necessary.",
                        REASONABLE,
                        "This states the disagreement but skips the specific near-miss and prior pushback that make the stakes and difficulty of persuading her feel real.",
                    ),
                    choice(
                        "communication-dev-situation-c",
                        "I say product managers and engineers sometimes see risk differently.",
                        WEAK,
                        "This generalizes into an abstract dynamic rather than describing the one specific disagreement and evidence you were working with.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "communication-dev-task-a",
                        "I say my task was to change her mind using evidence she'd find compelling, since simply restating that I thought it was risky hadn't worked the two previous times I'd raised it.",
                        STRONG,
                        "Recognizing that repeating your own opinion had already failed twice, and adjusting your approach rather than just trying harder the same way, shows real judgment about persuasion.",
                    ),
                    choice(
                        "communication-dev-task-b",
                        "I say my task was to convince the PM to add the confirmation step.",
                        REASONABLE,
                        "This is a fair goal, but it doesn't acknowledge that your previous approach to making this case hadn't worked, which is the harder part of the task.",
                    ),
                    choice(
                        "communication-dev-task-c",
                        "I say my task was to add the confirmation step anyway since I was confident I was right.",
                        WEAK,
                        "Acting unilaterally against a decision the PM had already made twice, rather than persuading her, sidesteps the actual challenge the question is asking about.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "communication-dev-action-a",
                        "I describe pulling three more support tickets showing similar near-misses over the past quarter, building a one-page summary quantifying how rare but severe the failure was, and proposing a lightweight one-click undo instead of a full confirmation step, directly addressing her friction concern.",
                        STRONG,
                        "Bringing more evidence and proposing a specific compromise that addresses the other person's actual concern, rather than just repeating your original ask, is exactly what effective persuasion looks like.",
                    ),
                    choice(
                        "communication-dev-action-b",
                        "I showed her the one incident ticket and explained again why I thought the risk was serious.",
                        REASONABLE,
                        "This adds one piece of evidence, but repeating essentially the same case that hadn't worked twice before is unlikely to change the outcome on its own.",
                    ),
                    choice(
                        "communication-dev-action-c",
                        "I escalated to the engineering director to get the decision made without going through the PM again.",
                        WEAK,
                        "Bypassing the PM instead of attempting a stronger, evidence-based case first skips the actual persuasion the question is asking about and risks damaging the working relationship.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "communication-dev-result-a",
                        "I say the PM agreed to the undo-based compromise, it shipped, it caught a second near-miss three months later with no added friction reported from power users, and she referenced the data-driven approach approvingly in a later planning document.",
                        STRONG,
                        "A concrete agreement, a later real-world catch, and unprompted positive acknowledgment from the PM together make this a complete, verifiable result.",
                    ),
                    choice(
                        "communication-dev-result-b",
                        "I say the PM agreed to add some kind of safeguard after seeing the additional tickets.",
                        REASONABLE,
                        "This confirms persuasion worked but doesn't show what specifically was agreed to or whether it held up afterward, which weakens the credibility of the outcome.",
                    ),
                    choice(
                        "communication-dev-result-c",
                        "I say the PM eventually came around to my point of view.",
                        WEAK,
                        "This is vague about what changed her mind or what was actually decided, so it doesn't demonstrate your specific persuasion approach worked.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "communication-dev-reflection-a",
                        "I say I learned that persuading someone who disagrees works far better with a compromise that addresses their actual concern plus fresh evidence, not by repeating your own position more emphatically, and I now look for that kind of compromise before raising an objection a third time.",
                        STRONG,
                        "This names a specific, reusable technique - address their concern, don't just repeat yours - and confirms it's become a standing practice, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "communication-dev-reflection-b",
                        "I say I learned that showing data helps when you're trying to persuade someone.",
                        REASONABLE,
                        "This is true but fairly generic - it doesn't capture the more specific insight about addressing the other person's actual concern rather than just adding evidence.",
                    ),
                    choice(
                        "communication-dev-reflection-c",
                        "I say I learned that you can change people's minds if you keep trying.",
                        WEAK,
                        "This doesn't name what actually changed her mind, so it reads as generic persistence rather than a specific, transferable lesson.",
                    ),
                ),
            ),
        ),
    )

    private val projectImpactDevelopingStar = StarWorkout(
        id = "project-impact-onboarding-bottleneck-developing-star",
        category = PROJECT_IMPACT,
        difficulty = DEVELOPING,
        commonQuestion = "Tell me about a time you identified and fixed an inefficient process.",
        scenario = "You noticed that every new engineer joining the team spent their entire first week manually " +
            "requesting and waiting on seven different access grants one at a time before they could ship anything - " +
            "a process that spanned five separate systems and that nobody actually owned.",
        steps = listOf(
            star(
                SITUATION, "How do you set up the Situation?",
                listOf(
                    choice(
                        "project-impact-dev-situation-a",
                        "I describe noticing the pattern after my third new-hire buddy assignment in a row, timing it out to an average of 4.5 business days from first day to first commit, entirely blocked on sequential access requests across five systems nobody clearly owned.",
                        STRONG,
                        "Specific numbers (4.5 days, five systems, three occurrences) turn a vague sense that onboarding was slow into a concrete, measured problem worth fixing.",
                    ),
                    choice(
                        "project-impact-dev-situation-b",
                        "I say I noticed new engineers took a long time to get set up before they could actually start working.",
                        REASONABLE,
                        "This states the problem but skips the measurement and the specific cross-system structure that made it worth investigating further.",
                    ),
                    choice(
                        "project-impact-dev-situation-c",
                        "I say onboarding is sometimes slow at companies in general.",
                        WEAK,
                        "This generalizes into an observation about onboarding everywhere rather than describing the one specific, measured problem you noticed and acted on.",
                    ),
                ),
            ),
            star(
                TASK, "How do you frame the Task?",
                listOf(
                    choice(
                        "project-impact-dev-task-a",
                        "I say my task was to find the actual bottleneck across the whole five-system chain, not just the piece my own team owned, since fixing only my team's step wouldn't move the real number if the other systems still ran sequentially after it.",
                        STRONG,
                        "Framing the task around the full cross-system chain, rather than just the part you had direct control over, shows the harder judgment call this kind of impact question is testing.",
                    ),
                    choice(
                        "project-impact-dev-task-b",
                        "I say my task was to speed up the access request specific to my own team's systems.",
                        REASONABLE,
                        "This is a reasonable, achievable scope, but limiting it to your own team's piece likely wouldn't move the overall onboarding time much if the other systems stayed sequential.",
                    ),
                    choice(
                        "project-impact-dev-task-c",
                        "I say my task was to write a clearer onboarding checklist for new hires to follow.",
                        WEAK,
                        "A clearer checklist might help people navigate the process, but it doesn't address why the process itself took so long, which is the actual bottleneck.",
                    ),
                ),
            ),
            star(
                ACTION, "How do you frame the Action?",
                listOf(
                    choice(
                        "project-impact-dev-action-a",
                        "I describe mapping the full five-system chain, finding only two systems had a real technical dependency on each other while the other three could run in parallel, then working with each system owner to submit all requests on day one and automating the two that genuinely had to run in sequence.",
                        STRONG,
                        "Mapping the actual dependency chain before proposing a fix - and finding most of it could run in parallel - shows a rigorous, evidence-based method rather than an assumption-driven fix.",
                    ),
                    choice(
                        "project-impact-dev-action-b",
                        "I worked with my own team to make our system's access grant automatic and faster.",
                        REASONABLE,
                        "This is a genuine improvement, but fixing only one of five systems leaves the sequential bottleneck across the rest of the chain largely unaddressed.",
                    ),
                    choice(
                        "project-impact-dev-action-c",
                        "I built a comprehensive new onboarding portal covering every possible new-hire need, which took six weeks before anything shipped.",
                        WEAK,
                        "Investing six weeks in a broad, unscoped solution before shipping anything risks solving the wrong problem and delays the actual fix far longer than necessary.",
                    ),
                ),
            ),
            star(
                RESULT, "How do you frame the Result?",
                listOf(
                    choice(
                        "project-impact-dev-result-a",
                        "I say average time-to-first-commit dropped from 4.5 days to under a day and a half across the next six new hires, and the approach was adopted as the standard onboarding flow by two other engineering teams.",
                        STRONG,
                        "A precise before-and-after metric across multiple new hires, plus adoption elsewhere, makes the impact concrete and hard to dispute.",
                    ),
                    choice(
                        "project-impact-dev-result-b",
                        "I say new hires seemed to get set up noticeably faster after the change.",
                        REASONABLE,
                        "This confirms a positive direction but gives no measurable scale, so the interviewer can't judge how significant the actual improvement was.",
                    ),
                    choice(
                        "project-impact-dev-result-c",
                        "I say onboarding felt smoother based on informal feedback from a couple of new hires.",
                        WEAK,
                        "Anecdotal comments with no measurement at all is a weak way to close out a story whose whole premise was a measured, cross-system fix.",
                    ),
                ),
            ),
            star(
                REFLECTION, "How do you frame the Reflection?",
                listOf(
                    choice(
                        "project-impact-dev-reflection-a",
                        "I say I learned that fixing a cross-system bottleneck requires mapping the real dependency chain first, since intuition about what has to run sequentially is often wrong, and I now start any process-improvement effort by timing and mapping the actual chain before proposing a fix.",
                        STRONG,
                        "This names a specific, reusable method - map the real chain before assuming what's sequential - and confirms it's become a standing practice, which is strong evidence of real reflection.",
                    ),
                    choice(
                        "project-impact-dev-reflection-b",
                        "I say I learned that small process improvements can add up to a lot of saved time.",
                        REASONABLE,
                        "This is a fair takeaway but fairly generic - it doesn't capture the specific technique of mapping the dependency chain this situation taught you.",
                    ),
                    choice(
                        "project-impact-dev-reflection-c",
                        "I say I learned that onboarding is something companies should pay more attention to.",
                        WEAK,
                        "This is a general opinion about companies rather than a lesson about what you'd personally do differently next time.",
                    ),
                ),
            ),
        ),
    )

    /** All authored STAR workouts, across every authored [BehavioralCategory] x [PracticeDifficulty] tier so far. */
    val starWorkouts: List<StarWorkout> = listOf(
        leadershipStar,
        conflictStar,
        failureLearningStar,
        ownershipStar,
        teamworkStar,
        ambiguityStar,
        prioritizationStar,
        communicationStar,
        projectImpactStar,
        leadershipDevelopingStar,
        conflictDevelopingStar,
        failureLearningDevelopingStar,
        ownershipDevelopingStar,
        teamworkDevelopingStar,
        ambiguityDevelopingStar,
        prioritizationDevelopingStar,
        communicationDevelopingStar,
        projectImpactDevelopingStar,
    )

    // ---------------------------------------------------------------- single-decision behavioral exercises

    /**
     * One [InterviewExerciseMode.IMPROVE_AN_ANSWER], one [InterviewExerciseMode.SPOT_RED_FLAG], and one
     * [InterviewExerciseMode.HANDLE_FOLLOWUPS] exercise, one per category currently covered -
     * a growing initial set, not exhaustive coverage of every mode x category pair.
     */
    val behavioralExercises: List<BehavioralExercise> = listOf(
        BehavioralExercise(
            id = "leadership-improve-delegation",
            commonQuestion = "Tell me about a time you delegated effectively.",
            category = LEADERSHIP,
            mode = InterviewExerciseMode.IMPROVE_AN_ANSWER,
            scenario = "A candidate is asked: \"Tell me about a time you led a team through a difficult project.\"",
            referenceAnswer = "\"I led my team through a really difficult project last year. It was hard, " +
                "but we worked together and got it done, and everyone was happy with the result.\"",
            prompt = "Which choice best improves this answer?",
            choices = listOf(
                choice(
                    "leadership-improve-a",
                    "Add a specific example of one decision you made as the leader and one concrete way you supported a struggling teammate.",
                    STRONG,
                    "Concrete decisions and specific support you gave are exactly what's missing - they turn a vague summary into evidence of real leadership behavior.",
                ),
                choice(
                    "leadership-improve-b",
                    "Add more enthusiastic language about how proud the team was of the outcome.",
                    REASONABLE,
                    "This makes the delivery more energetic but doesn't add any of the missing specifics an interviewer actually needs to evaluate your leadership.",
                ),
                choice(
                    "leadership-improve-c",
                    "Shorten the answer since it's already clear enough.",
                    WEAK,
                    "The answer isn't too long - it's too vague; shortening it further would remove even more of the substance an interviewer is listening for.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "conflict-red-flag-escalation",
            commonQuestion = "Tell me about a time you disagreed with a decision at work.",
            category = CONFLICT,
            mode = InterviewExerciseMode.SPOT_RED_FLAG,
            scenario = "A candidate describes a disagreement with a coworker like this: \"I was right and they " +
                "were wrong, so I went straight to my manager to get them overruled, and my manager backed me up immediately.\"",
            prompt = "What's the biggest red flag in this answer?",
            choices = listOf(
                choice(
                    "conflict-redflag-a",
                    "It skips any attempt to resolve the disagreement directly with the coworker before escalating.",
                    STRONG,
                    "Interviewers listen for whether you try direct resolution first - jumping straight to escalation without that step suggests you might do the same with future teammates.",
                ),
                choice(
                    "conflict-redflag-b",
                    "It doesn't explain what the disagreement was actually about.",
                    REASONABLE,
                    "This is a real gap, but it's a smaller one - the bigger concern is the behavior pattern (escalate first) more than the missing technical detail.",
                ),
                choice(
                    "conflict-redflag-c",
                    "It doesn't mention how long the project took overall.",
                    WEAK,
                    "Project duration isn't relevant to evaluating how the disagreement itself was handled, so this isn't the flag the question is looking for.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "failure-followup-recurrence",
            commonQuestion = "Tell me about a time you missed a deadline.",
            category = FAILURE_LEARNING,
            mode = InterviewExerciseMode.HANDLE_FOLLOWUPS,
            scenario = "You just finished telling the interviewer about a time you missed a deadline and how " +
                "you handled it. The interviewer follows up: \"Has something similar happened again since then?\"",
            prompt = "Which response handles the follow-up best?",
            choices = listOf(
                choice(
                    "failure-followup-a",
                    "\"Yes, once - and that time I flagged the risk a week earlier than I would have before, which gave the team enough time to adjust the plan.\"",
                    STRONG,
                    "Admitting it happened again but showing the specific behavior change is more convincing than claiming perfection - it proves the lesson actually stuck.",
                ),
                choice(
                    "failure-followup-b",
                    "\"No, I've made sure that never happens again.\"",
                    REASONABLE,
                    "This sounds confident, but an absolute claim like 'never again' is hard to believe and can read as overselling rather than honest reflection.",
                ),
                choice(
                    "failure-followup-c",
                    "\"I try not to think about it too much since it wasn't a big deal.\"",
                    WEAK,
                    "Downplaying the follow-up suggests you haven't actually reflected on whether the lesson stuck, which is exactly what this question is testing.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "ownership-improve-bug-postmortem",
            commonQuestion = "Tell me about a time you owned up to a mistake.",
            category = OWNERSHIP,
            mode = InterviewExerciseMode.IMPROVE_AN_ANSWER,
            scenario = "A candidate is asked: \"Tell me about a time you made a mistake at work.\"",
            referenceAnswer = "\"I made a mistake once that caused some problems, but I fixed it and everything " +
                "was fine after that.\"",
            prompt = "Which choice best improves this answer?",
            choices = listOf(
                choice(
                    "ownership-improve-a",
                    "Add the specific mistake, how it was discovered, and one concrete action taken to fix and prevent it from recurring.",
                    STRONG,
                    "Naming the specific mistake and the concrete fix is exactly what's missing - it turns a vague admission into evidence of real accountability.",
                ),
                choice(
                    "ownership-improve-b",
                    "Add a line about how stressful it was to deal with the mistake at the time.",
                    REASONABLE,
                    "This adds emotional color but doesn't give the interviewer any of the specifics needed to judge how you actually handled the mistake.",
                ),
                choice(
                    "ownership-improve-c",
                    "Remove the mention of a mistake and instead describe a project that went well.",
                    WEAK,
                    "Swapping in a success story avoids the question entirely - interviewers ask this specifically to see how you handle and own failure, not to hear about a win.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "teamwork-red-flag-solo-credit",
            commonQuestion = "Tell me about a time you worked closely with a team to deliver something.",
            category = TEAMWORK,
            mode = InterviewExerciseMode.SPOT_RED_FLAG,
            scenario = "A candidate describes a group project like this: \"The project was mostly me carrying the " +
                "team - I ended up redoing most of what my two teammates built because it wasn't up to my " +
                "standard, and I presented it as my own work since I'd done the real work anyway.\"",
            prompt = "What's the biggest red flag in this answer?",
            choices = listOf(
                choice(
                    "teamwork-redflag-a",
                    "Presenting teammates' contributions as solely the candidate's own work after redoing it, instead of acknowledging what the team actually built together.",
                    STRONG,
                    "Claiming sole credit for team output, even after redoing parts of it, signals a pattern that could seriously damage trust with future teammates - this is the core issue an interviewer would flag.",
                ),
                choice(
                    "teamwork-redflag-b",
                    "Redoing teammates' work without describing whether they were told why or given a chance to fix it themselves first.",
                    REASONABLE,
                    "This is a real concern about how the situation was handled, but it's secondary to the bigger issue of claiming the work as entirely your own afterward.",
                ),
                choice(
                    "teamwork-redflag-c",
                    "Not mentioning how long the project took overall.",
                    WEAK,
                    "Project duration isn't relevant to evaluating how the candidate treated their teammates, so this isn't the flag the question is looking for.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "ambiguity-followup-wrong-guess",
            commonQuestion = "Tell me about a time you made a decision without complete information.",
            category = AMBIGUITY,
            mode = InterviewExerciseMode.HANDLE_FOLLOWUPS,
            scenario = "You just finished describing a time you had to scope a vague request yourself. The " +
                "interviewer follows up: \"What would you have done if your interpretation of the request had " +
                "turned out to be wrong?\"",
            prompt = "Which response handles the follow-up best?",
            choices = listOf(
                choice(
                    "ambiguity-followup-a",
                    "\"I checked in with the stakeholder with a one-line summary of my interpretation before building anything substantial, specifically so I'd find out early if I'd guessed wrong rather than after the work was done.\"",
                    STRONG,
                    "This shows the candidate built in a cheap early checkpoint specifically to catch a wrong guess, which directly answers the risk the interviewer is probing for.",
                ),
                choice(
                    "ambiguity-followup-b",
                    "\"I would have just adjusted my approach once I found out and redone the parts that were off.\"",
                    REASONABLE,
                    "This is a reasonable fallback, but it accepts finding out late as the default rather than describing how you'd try to catch a wrong guess earlier.",
                ),
                choice(
                    "ambiguity-followup-c",
                    "\"I was pretty confident in my interpretation, so I don't think that would have happened.\"",
                    WEAK,
                    "This dismisses a real risk the interviewer is asking about rather than engaging with it, and overconfidence about an inherently ambiguous request is itself a small red flag.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "prioritization-improve-competing-requests",
            commonQuestion = "Tell me about a time you had to say no to an important stakeholder request.",
            category = PRIORITIZATION,
            mode = InterviewExerciseMode.IMPROVE_AN_ANSWER,
            scenario = "A candidate is asked: \"Tell me about a time you had to juggle multiple priorities.\"",
            referenceAnswer = "\"I had a lot going on at once, but I managed to juggle everything and get it all " +
                "done in the end.\"",
            prompt = "Which choice best improves this answer?",
            choices = listOf(
                choice(
                    "prioritization-improve-a",
                    "Add what the specific competing priorities were, the reasoning used to sequence them, and how that sequence was communicated to the people affected.",
                    STRONG,
                    "The reasoning behind the sequencing is the real substance of a prioritization story - without it, 'juggling everything' says nothing about how the decision was actually made.",
                ),
                choice(
                    "prioritization-improve-b",
                    "Add a comment about how good multitasking skills are what got everything done.",
                    REASONABLE,
                    "This asserts a trait rather than showing the actual decision-making process, which is what an interviewer needs to evaluate real prioritization judgment.",
                ),
                choice(
                    "prioritization-improve-c",
                    "Remove the detail about there being multiple priorities and focus on just one task instead.",
                    WEAK,
                    "Narrowing to a single task avoids the actual prioritization question - the interviewer specifically wants to hear how competing demands were weighed against each other.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "communication-red-flag-jargon-dump",
            commonQuestion = "Tell me about a time you explained a technical concept to a non-technical audience.",
            category = COMMUNICATION,
            mode = InterviewExerciseMode.SPOT_RED_FLAG,
            scenario = "A candidate describes explaining a delay to a non-technical client like this: \"I walked " +
                "them through the exact root cause - a race condition in our distributed cache invalidation logic " +
                "causing eventual consistency violations - so they'd understand it wasn't something we could have " +
                "easily prevented.\"",
            prompt = "What's the biggest red flag in this answer?",
            choices = listOf(
                choice(
                    "communication-redflag-a",
                    "Explaining a technical root cause in jargon a non-technical audience can't follow, instead of translating it into terms and implications they can act on.",
                    STRONG,
                    "Communication with a non-technical stakeholder is judged by whether they walk away able to make a decision, not by how precisely the technical detail was conveyed - dense jargon defeats that purpose.",
                ),
                choice(
                    "communication-redflag-b",
                    "Framing the explanation around proving it wasn't preventable rather than around what happens next.",
                    REASONABLE,
                    "This is a fair concern - the framing does lean defensive - but the more fundamental problem is that the explanation itself wasn't accessible to the audience at all.",
                ),
                choice(
                    "communication-redflag-c",
                    "Not mentioning how long the investigation into the root cause took.",
                    WEAK,
                    "How long the investigation took isn't relevant to whether the client-facing explanation itself was clear, so this isn't the flag the question is looking for.",
                ),
            ),
        ),
        BehavioralExercise(
            id = "project-impact-followup-attribution",
            commonQuestion = "Tell me about a time your work had a measurable, quantifiable impact.",
            category = PROJECT_IMPACT,
            mode = InterviewExerciseMode.HANDLE_FOLLOWUPS,
            scenario = "You just finished describing a project that improved a key metric significantly. The " +
                "interviewer follows up: \"How do you know the improvement was actually caused by your change, " +
                "and not something else happening at the same time?\"",
            prompt = "Which response handles the follow-up best?",
            choices = listOf(
                choice(
                    "project-impact-followup-a",
                    "\"We rolled it out to half of users first and compared the metric against the untouched half over the same two weeks, so the difference between the two groups is what I'm attributing to the change, not just the before-and-after trend.\"",
                    STRONG,
                    "Describing an actual comparison group is the strongest possible answer to an attribution question - it shows the impact claim was designed to be verifiable, not just correlated.",
                ),
                choice(
                    "project-impact-followup-b",
                    "\"I checked that no other major changes shipped around the same time, so I'm confident it was this change.\"",
                    REASONABLE,
                    "This is a reasonable sanity check, but ruling out other known changes is weaker evidence than an actual controlled comparison, and it can still miss factors you didn't think to check.",
                ),
                choice(
                    "project-impact-followup-c",
                    "\"The metric moved right after the change went out, so it's pretty clear it was the cause.\"",
                    WEAK,
                    "Timing alone doesn't establish causation - an interviewer asking this follow-up is specifically testing whether you understand that correlation isn't enough.",
                ),
            ),
        ),
    )

    // ---------------------------------------------------------------- technical communication

    private fun techComm(
        idSuffix: String,
        problemSlug: String,
        kind: TechCommExerciseKind,
        prompt: String,
        choices: List<RatedChoice>,
        difficulty: PracticeDifficulty = PracticeDifficulty.BEGINNER,
    ) = TechCommItem(id = "$problemSlug-$idSuffix", problemSlug = problemSlug, kind = kind, difficulty = difficulty, prompt = prompt, choices = choices)

    /** Technical Communication items, tied only to problems already authored elsewhere in the roadmap content. */
    val techCommItems: List<TechCommItem> = listOf(
        techComm(
            "clarify", "two-sum", CLARIFY_PROBLEM,
            "Before jumping into a solution for Two Sum, which clarifying question is most useful to ask the interviewer?",
            listOf(
                choice(
                    "two-sum-tc-clarify-a",
                    "\"Can I assume there's always exactly one valid pair, or should my solution handle the case where none exists?\"",
                    STRONG,
                    "This surfaces a real ambiguity that changes what your solution needs to return, showing you think about edge cases before writing anything.",
                ),
                choice(
                    "two-sum-tc-clarify-b",
                    "\"Can you repeat the problem statement?\"",
                    REASONABLE,
                    "This is a safe way to buy a moment to think, but it doesn't demonstrate any actual analysis of the problem the way a targeted clarifying question does.",
                ),
                choice(
                    "two-sum-tc-clarify-c",
                    "\"Is this problem hard?\"",
                    WEAK,
                    "This doesn't help you solve the problem or clarify requirements - it reads as stalling rather than genuine clarification.",
                ),
            ),
        ),
        techComm(
            "approach", "two-sum", EXPLAIN_APPROACH,
            "Which explanation best communicates your approach before you start coding?",
            listOf(
                choice(
                    "two-sum-tc-approach-a",
                    "\"I'll scan the array once, and for each number check whether its complement is already in a hash map; if not, I'll add the current number and move on.\"",
                    STRONG,
                    "This states the core idea, the data structure, and the check-then-insert order clearly enough that an interviewer can follow along without seeing code yet.",
                ),
                choice(
                    "two-sum-tc-approach-b",
                    "\"I'll use a hash map to solve it efficiently.\"",
                    REASONABLE,
                    "This names the right tool but skips how it's actually used, leaving the interviewer without a clear picture of your algorithm.",
                ),
                choice(
                    "two-sum-tc-approach-c",
                    "\"I'll just start coding and explain as I go.\"",
                    WEAK,
                    "Skipping the verbal approach entirely makes it hard for the interviewer to follow your reasoning and catch a wrong direction early.",
                ),
            ),
        ),
        techComm(
            "tradeoffs", "two-sum", DISCUSS_TRADEOFFS,
            "An interviewer asks why you didn't sort the array first. What's the strongest response?",
            listOf(
                choice(
                    "two-sum-tc-tradeoffs-a",
                    "\"Sorting would take O(n log n) and also lose the original indices, which the problem needs - a hash map gets O(n) time while keeping index lookups intact.\"",
                    STRONG,
                    "This directly compares both approaches on the two things that actually matter here - time complexity and whether indices survive - which is exactly what a tradeoffs discussion should do.",
                ),
                choice(
                    "two-sum-tc-tradeoffs-b",
                    "\"Sorting felt like more work to implement.\"",
                    REASONABLE,
                    "This is honest but doesn't engage with the actual technical tradeoff the interviewer is asking about.",
                ),
                choice(
                    "two-sum-tc-tradeoffs-c",
                    "\"I just always use hash maps for this kind of problem.\"",
                    WEAK,
                    "This reads as a habit rather than a reasoned choice, and it doesn't show the comparative thinking the question is looking for.",
                ),
            ),
        ),
        techComm(
            "edgecases", "two-sum", EXPLAIN_EDGE_CASES,
            "Which edge case is most important to mention out loud before finishing?",
            listOf(
                choice(
                    "two-sum-tc-edge-a",
                    "\"If the same value appears twice and together they're the answer, my solution still works because I check the complement before inserting the current value.\"",
                    STRONG,
                    "This calls out a genuinely tricky case (duplicate values forming the answer) and explains specifically why the approach handles it - exactly the kind of edge case worth saying out loud.",
                ),
                choice(
                    "two-sum-tc-edge-b",
                    "\"I should make sure the array isn't empty.\"",
                    REASONABLE,
                    "This is a reasonable general habit, but the problem guarantees a valid pair exists, so this particular edge case isn't the sharpest one to raise here.",
                ),
                choice(
                    "two-sum-tc-edge-c",
                    "\"I don't think there are any edge cases for this one.\"",
                    WEAK,
                    "Claiming there are no edge cases signals you haven't thought it through - even simple problems like this one have at least one worth mentioning.",
                ),
            ),
        ),
        techComm(
            "clarify", "contains-duplicate", CLARIFY_PROBLEM,
            "Which clarifying question is most useful before solving Contains Duplicate?",
            listOf(
                choice(
                    "contains-duplicate-tc-clarify-a",
                    "\"Should I optimize for the fewest comparisons, or is using extra memory for speed acceptable?\"",
                    STRONG,
                    "This surfaces the real design decision (time vs. space) up front, which shapes which approach you'll propose.",
                ),
                choice(
                    "contains-duplicate-tc-clarify-b",
                    "\"What programming language should I use?\"",
                    REASONABLE,
                    "This is a fair logistical question, but it doesn't clarify anything about the problem itself.",
                ),
                choice(
                    "contains-duplicate-tc-clarify-c",
                    "\"Can I look this problem up online first?\"",
                    WEAK,
                    "This isn't appropriate in an interview setting and doesn't demonstrate any problem-solving on your part.",
                ),
            ),
        ),
        techComm(
            "approach", "contains-duplicate", EXPLAIN_APPROACH,
            "Which explanation best communicates your approach?",
            listOf(
                choice(
                    "contains-duplicate-tc-approach-a",
                    "\"I'll walk through the array once, checking each value against a set of what I've already seen - if it's already there, I return true immediately.\"",
                    STRONG,
                    "This clearly states the data structure, the check, and the early exit, giving the interviewer a complete picture before any code appears.",
                ),
                choice(
                    "contains-duplicate-tc-approach-b",
                    "\"I'll check if there are any duplicates using a set.\"",
                    REASONABLE,
                    "This names the right tool but doesn't explain the actual mechanics of how you'll use it.",
                ),
                choice(
                    "contains-duplicate-tc-approach-c",
                    "\"I'll compare every number to every other number.\"",
                    WEAK,
                    "Stating the brute-force approach without acknowledging its cost, when a faster approach is expected, signals you may not know it's suboptimal.",
                ),
            ),
        ),
        techComm(
            "tradeoffs", "contains-duplicate", DISCUSS_TRADEOFFS,
            "An interviewer asks about the memory cost of your approach. What's the strongest response?",
            listOf(
                choice(
                    "contains-duplicate-tc-tradeoffs-a",
                    "\"In the worst case, with no duplicates, the set grows to hold every value, so it's O(n) extra space - the tradeoff is using that memory to get O(n) time instead of O(n squared).\"",
                    STRONG,
                    "This names the exact worst case, states the space cost precisely, and frames it as a deliberate tradeoff against the brute-force alternative - a complete answer.",
                ),
                choice(
                    "contains-duplicate-tc-tradeoffs-b",
                    "\"It uses a bit of extra memory, but not too much.\"",
                    REASONABLE,
                    "This gestures at the right idea but isn't precise enough - 'not too much' doesn't tell the interviewer anything they can evaluate.",
                ),
                choice(
                    "contains-duplicate-tc-tradeoffs-c",
                    "\"Memory usually isn't something I worry about.\"",
                    WEAK,
                    "Dismissing the question entirely misses a chance to show you understand the tradeoff you're making.",
                ),
            ),
        ),
        techComm(
            "edgecases", "contains-duplicate", EXPLAIN_EDGE_CASES,
            "Which edge case is most worth raising before finishing?",
            listOf(
                choice(
                    "contains-duplicate-tc-edge-a",
                    "\"An empty array should return false, since there's nothing to duplicate - my loop just never runs, so that falls out naturally.\"",
                    STRONG,
                    "This names a real edge case, states the expected behavior, and explains why the code already handles it - complete and specific.",
                ),
                choice(
                    "contains-duplicate-tc-edge-b",
                    "\"I should double check my code compiles.\"",
                    REASONABLE,
                    "This is a reasonable general habit, but it isn't a case about the problem's behavior, which is what 'edge case' is asking for here.",
                ),
                choice(
                    "contains-duplicate-tc-edge-c",
                    "\"This problem is simple enough that edge cases don't really matter.\"",
                    WEAK,
                    "Even simple problems have at least one boundary worth naming - dismissing edge cases outright is itself a red flag in a communication-focused exercise.",
                ),
            ),
        ),
        techComm(
            "clarify-dev", "two-sum", CLARIFY_PROBLEM,
            "Before settling on your hash map approach for Two Sum, which clarifying question shows the deepest understanding of the problem's constraints?",
            listOf(
                choice(
                    "two-sum-tc-clarify-dev-a",
                    "\"The problem guarantees exactly one valid answer - does that guarantee still hold even if the array contains duplicate values that could otherwise form more than one valid pair?\"",
                    STRONG,
                    "This probes a subtler edge in the stated guarantee itself - whether duplicates could threaten uniqueness - which shows a deeper read of the constraints than just accepting them at face value.",
                ),
                choice(
                    "two-sum-tc-clarify-dev-b",
                    "\"Is the array guaranteed to be sorted, or could it be in any order?\"",
                    REASONABLE,
                    "This is a fair question to ask, but since a hash map approach works regardless of order, the answer wouldn't actually change your chosen approach.",
                ),
                choice(
                    "two-sum-tc-clarify-dev-c",
                    "\"How many test cases will you be running against my solution?\"",
                    WEAK,
                    "This doesn't clarify anything about the problem's requirements or constraints - it reads as stalling rather than genuine analysis.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "approach-dev", "two-sum", EXPLAIN_APPROACH,
            "An interviewer says: \"Walk me through exactly why you check the map before inserting, not after.\" What's the strongest explanation?",
            listOf(
                choice(
                    "two-sum-tc-approach-dev-a",
                    "\"If I inserted first, a single element that happens to equal half the target would incorrectly pair with itself, since its own complement would already be sitting in the map - checking before inserting guarantees I only match against elements seen strictly earlier.\"",
                    STRONG,
                    "This identifies the precise failure mode the check-then-insert order prevents, which is exactly the kind of reasoning that shows real understanding of the approach rather than memorized syntax.",
                ),
                choice(
                    "two-sum-tc-approach-dev-b",
                    "\"Checking first is just how I remember to write it, and it's worked in the examples I've tried.\"",
                    REASONABLE,
                    "This is honest, but relying on habit and example-testing rather than explaining the underlying reason doesn't demonstrate real understanding of why the order matters.",
                ),
                choice(
                    "two-sum-tc-approach-dev-c",
                    "\"It doesn't really matter which order I do it in, as long as both happen somewhere in the loop.\"",
                    WEAK,
                    "This is incorrect - the order directly determines whether a single self-pairing element is handled correctly, so dismissing it as unimportant signals a real gap in understanding.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "tradeoffs-dev", "two-sum", DISCUSS_TRADEOFFS,
            "An interviewer asks whether you'd change your approach if the array had 10 million elements and the answer was almost always found within the first few elements. What's the strongest response?",
            listOf(
                choice(
                    "two-sum-tc-tradeoffs-dev-a",
                    "\"My check-as-I-go approach already returns as soon as a pair is found, so for a large array where the answer tends to appear early, it's already close to constant time in the typical case - a version that builds a full lookup before scanning would waste that advantage.\"",
                    STRONG,
                    "This connects the algorithm's existing early-exit behavior to the specific scenario asked about, showing the candidate understands why their approach already handles it well rather than reaching for a change.",
                ),
                choice(
                    "two-sum-tc-tradeoffs-dev-b",
                    "\"I'd probably keep the hash map approach either way, since it's the standard efficient solution for this problem regardless of array size.\"",
                    REASONABLE,
                    "This lands on the right approach, but without explaining the early-exit reasoning, it doesn't show the interviewer why this specific approach handles the scenario well.",
                ),
                choice(
                    "two-sum-tc-tradeoffs-dev-c",
                    "\"I'd switch to a two-pointer approach instead since it uses less memory.\"",
                    WEAK,
                    "Switching would require sorting the array first, which costs O(n log n) and would erase the exact early-exit advantage that matters most in the scenario being asked about.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "edgecases-dev", "two-sum", EXPLAIN_EDGE_CASES,
            "Which edge case is the sharper one to mention for Two Sum at this stage?",
            listOf(
                choice(
                    "two-sum-tc-edge-dev-a",
                    "\"If a single element is exactly half the target and appears only once in the array, my solution won't incorrectly match it with itself, since I check the map before inserting the current value, not after.\"",
                    STRONG,
                    "This names a genuinely subtle failure mode - a self-pairing false positive - and explains precisely why the implementation avoids it, which is a sharper case than a purely structural one.",
                ),
                choice(
                    "two-sum-tc-edge-dev-b",
                    "\"If the array is very large, my hash map could end up using a meaningful amount of memory.\"",
                    REASONABLE,
                    "This is a real consideration, but it's about resource usage rather than correctness, which is a less sharp edge case than one that could cause a wrong answer.",
                ),
                choice(
                    "two-sum-tc-edge-dev-c",
                    "\"If the numbers involved are all small, like single digits, my solution should still work fine.\"",
                    WEAK,
                    "Small values don't create any distinct risk for this approach, so raising it doesn't demonstrate meaningful edge-case thinking.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "clarify-dev", "contains-duplicate", CLARIFY_PROBLEM,
            "Which clarifying question shows the deepest understanding of the space/time tradeoff for Contains Duplicate?",
            listOf(
                choice(
                    "contains-duplicate-tc-clarify-dev-a",
                    "\"Am I allowed to modify the input array in place? If so, sorting first would let me check adjacent elements for duplicates in O(1) extra space instead of using a hash set.\"",
                    STRONG,
                    "This surfaces a real design fork (mutate for less memory vs. hash set for less time) that a candidate wouldn't know to consider without asking, showing deeper command of the tradeoff space.",
                ),
                choice(
                    "contains-duplicate-tc-clarify-dev-b",
                    "\"Is the array already guaranteed to be sorted?\"",
                    REASONABLE,
                    "This is a fair question, but since a hash set approach doesn't depend on ordering, the answer wouldn't meaningfully change your chosen approach.",
                ),
                choice(
                    "contains-duplicate-tc-clarify-dev-c",
                    "\"Should I write this recursively instead of with a loop?\"",
                    WEAK,
                    "This is a stylistic implementation detail unrelated to the problem's actual constraints or tradeoffs, so it doesn't demonstrate meaningful clarification.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "approach-dev", "contains-duplicate", EXPLAIN_APPROACH,
            "An interviewer asks you to explain why you chose a hash set over sorting first. What's the strongest explanation?",
            listOf(
                choice(
                    "contains-duplicate-tc-approach-dev-a",
                    "\"Sorting is O(n log n) and I'd have to scan through the whole thing regardless of where a duplicate falls; the hash set lets me return true the moment I hit a repeat, which matters if duplicates tend to appear early in the array.\"",
                    STRONG,
                    "This compares both approaches on the dimension that actually matters for the scenario - how quickly each can exit early - rather than just naming a complexity difference in the abstract.",
                ),
                choice(
                    "contains-duplicate-tc-approach-dev-b",
                    "\"A hash set gives me O(n) time instead of O(n log n) for sorting, so it's generally faster.\"",
                    REASONABLE,
                    "This states the correct complexity comparison, but it misses the more specific early-exit reasoning that makes the hash set the better choice in this scenario.",
                ),
                choice(
                    "contains-duplicate-tc-approach-dev-c",
                    "\"I just default to hash sets any time I need to check for duplicates.\"",
                    WEAK,
                    "This reads as a habit rather than a reasoned choice, and it doesn't show the comparative thinking the question is actually asking for.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "tradeoffs-dev", "contains-duplicate", DISCUSS_TRADEOFFS,
            "An interviewer says memory is extremely constrained in this environment and asks how you'd adapt. What's the strongest response?",
            listOf(
                choice(
                    "contains-duplicate-tc-tradeoffs-dev-a",
                    "\"I'd switch to sorting the array in place and checking adjacent elements, which drops extra space to O(1) at the cost of O(n log n) time instead of O(n) - a fair trade if memory is the binding constraint.\"",
                    STRONG,
                    "This names a specific alternative approach and precisely states what's being traded (time for space), which is exactly what an adaptive tradeoffs discussion should demonstrate.",
                ),
                choice(
                    "contains-duplicate-tc-tradeoffs-dev-b",
                    "\"I'd try to reduce memory some other way but probably keep the hash set since it's simpler to reason about.\"",
                    REASONABLE,
                    "This acknowledges the constraint but doesn't offer a concrete alternative that actually addresses it, leaving the tradeoff unresolved.",
                ),
                choice(
                    "contains-duplicate-tc-tradeoffs-dev-c",
                    "\"I'd keep the same approach since a little extra memory usually isn't a big deal.\"",
                    WEAK,
                    "This dismisses a constraint the interviewer explicitly stated was extreme, rather than engaging with how you'd actually adapt to it.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
        techComm(
            "edgecases-dev", "contains-duplicate", EXPLAIN_EDGE_CASES,
            "Which edge case is the sharper one to mention for Contains Duplicate at this stage?",
            listOf(
                choice(
                    "contains-duplicate-tc-edge-dev-a",
                    "\"If every element in the array is identical, my early-exit check should return true immediately on the second element rather than needing to scan the whole array, which is worth calling out since it shows the early exit actually matters.\"",
                    STRONG,
                    "This names a case that demonstrates a specific performance property of the implementation, not just a correctness boundary, which is a sharper observation than a purely structural edge case.",
                ),
                choice(
                    "contains-duplicate-tc-edge-dev-b",
                    "\"If the array contains negative numbers mixed with positive ones, my hash set approach still works normally since it doesn't rely on any numeric range or sign.\"",
                    REASONABLE,
                    "This is true and worth knowing, but it's a fairly obvious property of hash sets rather than a case that reveals something sharper about the specific implementation.",
                ),
                choice(
                    "contains-duplicate-tc-edge-dev-c",
                    "\"If the array is unsorted, my solution should still work correctly.\"",
                    WEAK,
                    "A hash set approach never depended on ordering in the first place, so this isn't really an edge case worth raising at all.",
                ),
            ),
            difficulty = DEVELOPING,
        ),
    )

    // ---------------------------------------------------------------- mock interview

    /** The one polished, complete BEGINNER-tier mock interview - see [MockInterview]'s doc comment for why feedback is deferred. */
    private val crossTeamRolloutMock: MockInterview = MockInterview(
        id = "mock-cross-team-rollout",
        title = "The Cross-Team Rollout",
        setup = "You're in a behavioral interview. The interviewer says: \"Walk me through a project where " +
            "things didn't go as planned, and I'll ask a few follow-ups as we go.\" You start describing a " +
            "rollout of a new internal tool across three teams that you were coordinating.",
        decisions = listOf(
            MockInterviewDecision(
                id = "mock-rollout-d1",
                order = 1,
                prompt = "The interviewer asks: \"What was your specific role in this rollout?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-rollout-d1-a",
                        "\"I was the one coordinating the rollout across all three teams - scheduling the migrations, tracking blockers, and making the call on sequencing.\"",
                        STRONG,
                        "Naming specific, ownable responsibilities right away gives the interviewer something concrete to follow up on for the rest of the story.",
                    ),
                    choice(
                        "mock-rollout-d1-b",
                        "\"I was part of the team working on the rollout.\"",
                        REASONABLE,
                        "This is accurate but vague about what you specifically did, which makes the rest of the story harder to anchor to your actions.",
                    ),
                    choice(
                        "mock-rollout-d1-c",
                        "\"I was mostly just helping out where needed.\"",
                        WEAK,
                        "This undersells your role and gives the interviewer little to hold onto - vague framing this early makes the whole story harder to follow.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-rollout-d2",
                order = 2,
                prompt = "The interviewer asks: \"What went wrong?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-rollout-d2-a",
                        "\"One team's migration depended on an API change from another team that got delayed, and I hadn't built in a way to catch that dependency early.\"",
                        STRONG,
                        "This names a specific, ownable gap in your own process rather than blaming the other team, which is exactly the honesty interviewers look for here.",
                    ),
                    choice(
                        "mock-rollout-d2-b",
                        "\"The timeline turned out to be too aggressive for everyone.\"",
                        REASONABLE,
                        "This is a fair observation but frames the problem as external circumstance rather than something in your own process you could have caught.",
                    ),
                    choice(
                        "mock-rollout-d2-c",
                        "\"Another team didn't deliver what they promised on time.\"",
                        WEAK,
                        "Placing the failure entirely on another team, without acknowledging your own role in catching or managing that risk, reads as deflecting responsibility.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-rollout-d3",
                order = 3,
                prompt = "The interviewer asks: \"Once you realized this, what did you do?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-rollout-d3-a",
                        "\"I mapped out every remaining dependency across the three teams that same day, flagged the two riskiest ones to my manager, and rebuilt the schedule around them instead of the original plan.\"",
                        STRONG,
                        "Concrete, immediate, and specific actions like this show you took control of the problem the moment you saw it, rather than waiting to see how it played out.",
                    ),
                    choice(
                        "mock-rollout-d3-b",
                        "\"I talked to the team that was delayed to understand what was going on.\"",
                        REASONABLE,
                        "This is a reasonable first step, but on its own it addresses only one part of the problem, not the broader risk across all three teams.",
                    ),
                    choice(
                        "mock-rollout-d3-c",
                        "\"I waited to see if the delayed team could catch up on their own.\"",
                        WEAK,
                        "Waiting rather than acting once you'd identified the risk suggests passivity at exactly the moment initiative was needed.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-rollout-d4",
                order = 4,
                prompt = "The interviewer asks: \"How did the other teams react to the schedule change?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-rollout-d4-a",
                        "\"I walked each team lead through the new sequencing and why, took their concerns seriously, and adjusted two dates based on feedback from the team most affected.\"",
                        STRONG,
                        "This shows you treated the schedule change as a conversation, not a decree - incorporating real feedback is what makes cross-team coordination credible.",
                    ),
                    choice(
                        "mock-rollout-d4-b",
                        "\"I sent out an email explaining the new schedule.\"",
                        REASONABLE,
                        "This communicates the decision but is one-directional - it doesn't show you engaged with how the affected teams actually felt about it.",
                    ),
                    choice(
                        "mock-rollout-d4-c",
                        "\"I didn't really check - I just needed the schedule to be final.\"",
                        WEAK,
                        "Prioritizing a final schedule over the affected teams' input risks damaging trust, even if the schedule itself was reasonable.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-rollout-d5",
                order = 5,
                prompt = "The interviewer asks: \"Looking back, what would you do differently?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-rollout-d5-a",
                        "\"I'd map cross-team dependencies explicitly at the start of any rollout now, not just track my own team's timeline - that single gap is what caused the delay.\"",
                        STRONG,
                        "This names one precise, structural change you've actually made, tied directly to the root cause you identified earlier - the strongest kind of reflection.",
                    ),
                    choice(
                        "mock-rollout-d5-b",
                        "\"I'd try to communicate more proactively with other teams.\"",
                        REASONABLE,
                        "This is a reasonable lesson but general enough to follow almost any project story, rather than tied specifically to what actually went wrong here.",
                    ),
                    choice(
                        "mock-rollout-d5-c",
                        "\"Honestly, I think it went about as well as it could have.\"",
                        WEAK,
                        "Given you just described a real failure, claiming there's nothing you'd change reads as unreflective rather than confident.",
                    ),
                ),
            ),
        ),
    )

    /** The DEVELOPING-tier mock interview - a different narrative mix (ambiguity, stakeholder conflict, team strain) than [crossTeamRolloutMock]'s ownership/communication/prioritization thread. */
    private val shiftingRequirementsMock: MockInterview = MockInterview(
        id = "mock-shifting-requirements",
        title = "The Shifting Requirements",
        difficulty = DEVELOPING,
        setup = "You're in a behavioral interview. The interviewer says: \"Tell me about a project where the " +
            "requirements kept changing on you, and I'll ask a few follow-ups as we go.\" You start describing a " +
            "new analytics dashboard you built for a VP who changed his mind about the scope three times over " +
            "six weeks, with nothing written down to anchor against.",
        decisions = listOf(
            MockInterviewDecision(
                id = "mock-shifting-d1",
                order = 1,
                prompt = "The interviewer asks: \"What did the VP actually ask for initially?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-shifting-d1-a",
                        "\"He said he wanted 'better visibility into regional performance' - no metrics, no mockup, nothing written down - so I asked him to describe the last time he'd needed that visibility and couldn't get it, which gave me a real example to build from.\"",
                        STRONG,
                        "Turning a vague executive ask into a concrete example before building anything shows exactly the scoping instinct the interviewer is listening for.",
                    ),
                    choice(
                        "mock-shifting-d1-b",
                        "\"He asked for a dashboard showing regional performance, so I started sketching what that might look like.\"",
                        REASONABLE,
                        "This is a reasonable first step, but starting to sketch based on a vague ask, without first narrowing it down, leaves a lot of room for a later mismatch.",
                    ),
                    choice(
                        "mock-shifting-d1-c",
                        "\"He wasn't very specific, so I just built what I thought a good regional dashboard would include.\"",
                        WEAK,
                        "Filling the ambiguity with your own assumptions, rather than checking them against the actual stakeholder, is a risky way to start a project for someone senior and demanding.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-shifting-d2",
                order = 2,
                prompt = "The interviewer follows up: \"Three weeks in, he said it wasn't what he wanted after all. What happened?\"",
                choices = listOf(
                    choice(
                        "mock-shifting-d2-a",
                        "\"He'd shown an early version to his peers and gotten pushback that it didn't compare well against a competitor's report they'd seen, so the ask had quietly shifted from 'visibility' to 'competitive benchmarking' without him ever framing it that way to me.\"",
                        STRONG,
                        "Naming the specific external trigger behind the shift shows real insight into why the requirement changed, not just that it changed.",
                    ),
                    choice(
                        "mock-shifting-d2-b",
                        "\"He said the direction wasn't quite right and wanted to change some of what I'd built.\"",
                        REASONABLE,
                        "This states that the requirement changed but doesn't explain why, which is the detail that actually helps the interviewer judge how you'd anticipate it next time.",
                    ),
                    choice(
                        "mock-shifting-d2-c",
                        "\"He just changed his mind, which happens with executives sometimes.\"",
                        WEAK,
                        "Chalking it up to a personality trait rather than investigating the actual cause misses a chance to show real analytical thinking about why the scope moved.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-shifting-d3",
                order = 3,
                prompt = "The interviewer asks: \"How did you handle the scope changing again?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-shifting-d3-a",
                        "\"I asked to see the competitor report that had prompted the pushback so I understood the real comparison he cared about, then got a specific one-page written agreement on the new scope before rebuilding anything, instead of guessing at his intent a second time.\"",
                        STRONG,
                        "Getting to the underlying reference material and locking in a written agreement shows you adapted your process after the first round of ambiguity, not just your output.",
                    ),
                    choice(
                        "mock-shifting-d3-b",
                        "\"I rebuilt the dashboard based on his new verbal feedback.\"",
                        REASONABLE,
                        "This responds to the new direction, but rebuilding from verbal feedback again repeats the exact pattern that caused the mismatch the first time.",
                    ),
                    choice(
                        "mock-shifting-d3-c",
                        "\"I agreed to keep iterating live in meetings with him, building changes on the spot as he thought out loud.\"",
                        WEAK,
                        "Building live off unstructured thinking invites the scope to keep drifting indefinitely, rather than anchoring it to something concrete.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-shifting-d4",
                order = 4,
                prompt = "The interviewer asks: \"Your teammate who'd been helping you build this got frustrated with the repeated rework. How did you handle that?\"",
                choices = listOf(
                    choice(
                        "mock-shifting-d4-a",
                        "\"I acknowledged directly that the repeated rework had been frustrating, shared the written agreement so they could see the scope was now anchored, and split the remaining work so they weren't the one absorbing the next round of ambiguity alone.\"",
                        STRONG,
                        "Addressing the teammate's frustration directly and changing how the remaining risk is shared shows real awareness of the toll ambiguity takes on collaborators, not just on you.",
                    ),
                    choice(
                        "mock-shifting-d4-b",
                        "\"I told my teammate to hang in there since the project would be done soon.\"",
                        REASONABLE,
                        "This acknowledges the frustration but doesn't change anything about how the work or the risk was being shared, which is what would actually address it.",
                    ),
                    choice(
                        "mock-shifting-d4-c",
                        "\"I didn't say much to my teammate since it wasn't really about them, and kept managing the VP relationship on my own.\"",
                        WEAK,
                        "Ignoring a teammate's frustration and continuing to absorb the ambiguity alone risks damaging the working relationship and burning them out unnecessarily.",
                    ),
                ),
            ),
            MockInterviewDecision(
                id = "mock-shifting-d5",
                order = 5,
                prompt = "The interviewer asks: \"Looking back, what would you do differently from the start?\" How do you respond?",
                choices = listOf(
                    choice(
                        "mock-shifting-d5-a",
                        "\"I'd ask for one concrete example or existing reference - like the competitor report - right in the first conversation, since that's what actually anchored the scope once I finally saw it, and I now ask for that kind of reference immediately on any vague request.\"",
                        STRONG,
                        "This names a specific, transferable habit tied directly to what actually resolved the ambiguity in the story, which is the strongest kind of reflection to close a mock interview on.",
                    ),
                    choice(
                        "mock-shifting-d5-b",
                        "\"I'd try to get things in writing earlier in the process.\"",
                        REASONABLE,
                        "This is a fair takeaway, but it's generic enough to follow almost any shifting-requirements story without capturing the specific technique that actually worked here.",
                    ),
                    choice(
                        "mock-shifting-d5-c",
                        "\"Honestly, executives just change their minds a lot, so I'm not sure there was much I could've done differently.\"",
                        WEAK,
                        "Given the story just showed a specific technique that did anchor the scope, claiming there was nothing to learn undercuts your own account of what worked.",
                    ),
                ),
            ),
        ),
    )

    /** Every authored mock interview, across every difficulty tier. */
    val mockInterviews: List<MockInterview> = listOf(crossTeamRolloutMock, shiftingRequirementsMock)

    /** [difficulty]'s tier if authored for [category], else the nearest tier via [searchOrder]. */
    fun starWorkoutFor(category: BehavioralCategory, difficulty: PracticeDifficulty): StarWorkout? =
        difficulty.searchOrder().firstNotNullOfOrNull { tier ->
            starWorkouts.firstOrNull { it.category == category && it.difficulty == tier }
        }

    /** [difficulty]'s tier if authored for [category]/[mode], else the nearest tier via [searchOrder]. */
    fun behavioralExercisesFor(category: BehavioralCategory, mode: InterviewExerciseMode, difficulty: PracticeDifficulty): List<BehavioralExercise> =
        difficulty.searchOrder().firstNotNullOfOrNull { tier ->
            behavioralExercises.filter { it.category == category && it.mode == mode && it.difficulty == tier }.ifEmpty { null }
        }.orEmpty()

    /** [difficulty]'s tier if authored for [problemSlug], else the nearest tier via [searchOrder]. */
    fun techCommItemsFor(problemSlug: String, difficulty: PracticeDifficulty): List<TechCommItem> =
        difficulty.searchOrder().firstNotNullOfOrNull { tier ->
            techCommItems.filter { it.problemSlug == problemSlug && it.difficulty == tier }.ifEmpty { null }
        }.orEmpty()
}
