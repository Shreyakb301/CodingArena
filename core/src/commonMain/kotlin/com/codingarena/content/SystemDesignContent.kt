package com.codingarena.content

import com.codingarena.domain.model.SystemDesignCategory
import com.codingarena.domain.model.SystemDesignCategory.APIS
import com.codingarena.domain.model.SystemDesignCategory.DATABASES
import com.codingarena.domain.model.SystemDesignCategory.INFRASTRUCTURE
import com.codingarena.domain.model.SystemDesignCategory.SCALABILITY
import com.codingarena.domain.model.SystemDesignCategory.THEORY
import com.codingarena.domain.model.SystemDesignConcept
import com.codingarena.domain.model.SystemDesignQuestion

private fun choice(text: String, correct: Boolean, explanation: String) =
    com.codingarena.domain.model.SystemDesignChoice(text, correct, explanation)

/**
 * System design fundamentals: short concepts with a couple of check questions
 * each, in the same "explain, then check" shape as the roadmap lessons.
 *
 * Independent of the DSA content - this is deliberately just data, browsed
 * statelessly with no new persistence, so it can sit alongside the existing
 * app without touching anything it already does.
 */
object SystemDesignContent {

    val concepts: List<SystemDesignConcept> = listOf(
        SystemDesignConcept(
            id = "load-balancing",
            title = "Load Balancing",
            category = INFRASTRUCTURE,
            summary = "A load balancer sits in front of a pool of servers and spreads incoming " +
                "requests across them, so no single server carries all the traffic and the " +
                "system can keep serving requests even if one instance goes down.",
            keyPoints = listOf(
                "Distributes requests across multiple servers instead of one",
                "Lets you scale horizontally - add servers instead of enlarging one",
                "Health checks stop it routing to instances that are down or slow",
                "Can work at the DNS level, the transport level (L4), or the application level (L7)",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "load-balancing-problem",
                    prompt = "What problem does putting a load balancer in front of your servers mainly solve?",
                    choices = listOf(
                        choice(
                            "One server would take all the traffic and become a bottleneck or a single point of failure.",
                            true,
                            "Spreading requests across a pool means no single machine's capacity caps the whole system, and one instance failing doesn't take everything down.",
                        ),
                        choice(
                            "A write to one server would need to be copied to every other server.",
                            false,
                            "That's a replication problem, not a load-balancing one - a load balancer only routes requests, it doesn't sync data between servers.",
                        ),
                        choice(
                            "Users in distant regions would see slower page loads than nearby users.",
                            false,
                            "That's a latency-and-geography problem best solved by a CDN or regional deployments - a load balancer's servers can all still be in one place.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "load-balancing-health-checks",
                    prompt = "Why do load balancers run periodic health checks against the servers behind them?",
                    choices = listOf(
                        choice(
                            "So they stop sending requests to an instance that has crashed or is responding too slowly.",
                            true,
                            "Without health checks, the load balancer would keep routing a share of traffic to a dead or struggling instance, turning one bad server into failed requests for real users.",
                        ),
                        choice(
                            "So they can decide which server should hold the primary copy of the data.",
                            false,
                            "Picking a primary is a database-replication decision, not something a load balancer's health check is responsible for.",
                        ),
                        choice(
                            "So they can compress responses before sending them back to the client.",
                            false,
                            "Compression is a response-encoding concern, unrelated to whether a health check is passing or failing.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "load-balancing-sticky-sessions",
                    prompt = "An app stores a user's shopping cart in the memory of whichever server first handled their request. What breaks under a load balancer?",
                    choices = listOf(
                        choice(
                            "The next request might land on a different server that has never seen that cart, so it looks empty.",
                            true,
                            "In-memory state tied to one instance only exists there - once the load balancer sends a later request elsewhere, that server has no idea the cart exists.",
                        ),
                        choice(
                            "The load balancer would refuse to accept any request that carries cookies.",
                            false,
                            "Load balancers pass cookies through like any other header - they don't inspect or block based on cookie presence.",
                        ),
                        choice(
                            "The database would run out of connections faster than usual.",
                            false,
                            "Nothing about routing requests across servers changes how many database connections get opened - this scenario doesn't even involve the database.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "caching",
            title = "Caching",
            category = INFRASTRUCTURE,
            summary = "A cache keeps a copy of frequently-needed data somewhere much faster to read " +
                "from than its original source, so most requests never have to pay the cost of " +
                "recomputing or refetching it.",
            keyPoints = listOf(
                "Trades a little staleness risk for a lot of speed",
                "Works best on data that's read far more often than it changes",
                "Needs an eviction policy (like LRU) once the cache fills up",
                "Cache invalidation - knowing when a cached value is stale - is the hard part",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "caching-when",
                    prompt = "Which data is the best candidate to cache?",
                    choices = listOf(
                        choice(
                            "A product's name and description, which are read constantly and rarely edited.",
                            true,
                            "High read frequency plus low change frequency is exactly the shape that benefits from caching - most reads hit the fast cached copy, and invalidation happens rarely.",
                        ),
                        choice(
                            "A user's live account balance, which must reflect every transaction instantly.",
                            false,
                            "Data that must always be exactly current is a poor caching candidate - any staleness window risks showing a wrong balance right after a transaction.",
                        ),
                        choice(
                            "A one-time password that's read exactly once and then discarded.",
                            false,
                            "A value read only once gains nothing from caching - there's no repeated read for the cache to speed up before it's thrown away.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "caching-eviction",
                    prompt = "A cache has a fixed size and is full. What does an LRU (least recently used) eviction policy do next?",
                    choices = listOf(
                        choice(
                            "It removes whichever cached entry hasn't been accessed for the longest time.",
                            true,
                            "LRU assumes recently-used data is likely to be used again soon, so it frees space by discarding the entry that's gone the longest without a read.",
                        ),
                        choice(
                            "It removes the entry that took the longest time to originally compute or fetch.",
                            false,
                            "That describes a cost-aware policy, not LRU - LRU only tracks recency of access, not how expensive an entry was to produce.",
                        ),
                        choice(
                            "It doubles the cache's size automatically to make room for the new entry.",
                            false,
                            "Eviction policies free existing space rather than growing the cache - resizing would defeat the point of having a fixed, bounded cache.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "caching-stale-data",
                    prompt = "A user updates their profile photo, but their friends still see the old one for a few minutes. What's the most likely cause?",
                    choices = listOf(
                        choice(
                            "The old photo is still being served from a cache that hasn't been invalidated yet.",
                            true,
                            "This is the classic cache-invalidation problem: the underlying data changed, but the cached copy wasn't told to refresh, so reads keep returning the old value until it expires or is cleared.",
                        ),
                        choice(
                            "The load balancer is routing the friends to a different data center.",
                            false,
                            "Routing to a different data center wouldn't specifically cause a stale photo to persist - the symptom described points directly at cached data, not request routing.",
                        ),
                        choice(
                            "The database silently rejected the update because the image was too large.",
                            false,
                            "A rejected update would typically surface as an error to the user, not a delayed-but-eventually-consistent view for other people - that pattern is characteristic of caching.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "cdn",
            title = "Content Delivery Networks",
            category = INFRASTRUCTURE,
            summary = "A CDN is a network of servers spread across many physical locations that cache " +
                "and serve static content from whichever location is closest to the user, cutting " +
                "the distance data has to travel.",
            keyPoints = listOf(
                "Serves content from a location physically near the user, not your origin server",
                "Best suited to static assets: images, video, CSS, JS, downloads",
                "Reduces latency and takes load off the origin server",
                "Origin content still needs to propagate out to CDN edge nodes, which takes time",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "cdn-benefit",
                    prompt = "A site's users are spread across five continents, all served from one origin server in one country. What does putting static assets behind a CDN mainly improve?",
                    choices = listOf(
                        choice(
                            "Load time for users far from the origin, since assets are served from a nearby edge location instead.",
                            true,
                            "Physical distance is a big share of network latency - moving the copy closer to each user cuts the round-trip time regardless of how fast the origin server itself is.",
                        ),
                        choice(
                            "The consistency of data written to the origin's primary database.",
                            false,
                            "A CDN caches and serves content, it doesn't touch how the origin's database keeps its data consistent - those are separate concerns.",
                        ),
                        choice(
                            "The number of distinct user accounts the origin server can store.",
                            false,
                            "Account storage capacity is a database-scaling question, unrelated to where static files are cached and served from geographically.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "cdn-content-type",
                    prompt = "Which of these is the best fit for a CDN?",
                    choices = listOf(
                        choice(
                            "The product images and video thumbnails shown on every page of a shopping site.",
                            true,
                            "Static assets that are identical for every visitor and rarely change are exactly what CDN edge caching is built for - one cached copy serves every nearby user.",
                        ),
                        choice(
                            "A logged-in user's private order history, which differs for every account.",
                            false,
                            "Per-user, private data doesn't benefit from a shared edge cache the same way - it would need per-user caching logic, not a plain CDN in front of the origin.",
                        ),
                        choice(
                            "The result of a live database query that changes with every request.",
                            false,
                            "Content that's different on every single request defeats caching entirely - there's nothing stable for the CDN to store and reuse.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "db-replication",
            title = "Database Replication",
            category = DATABASES,
            summary = "Replication keeps copies of the same database on multiple servers. A common " +
                "setup is one primary that accepts writes and one or more replicas that accept " +
                "reads, so read traffic can scale out and a replica can take over if the primary fails.",
            keyPoints = listOf(
                "One primary handles writes; replicas hold copies for reads and failover",
                "Read-heavy apps scale by adding more read replicas",
                "Replication lag means a replica can briefly be behind the primary",
                "If the primary fails, a replica can be promoted to take its place",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "db-replication-purpose",
                    prompt = "An app is read-heavy and its single database server is struggling to keep up with query volume. What does adding read replicas do?",
                    choices = listOf(
                        choice(
                            "It spreads read queries across multiple copies of the database instead of one server handling them all.",
                            true,
                            "Each replica holds a full copy of the data and can answer reads independently, so the read load that used to hit one server is now shared across several.",
                        ),
                        choice(
                            "It splits each table's rows across servers so each one holds only part of the data.",
                            false,
                            "Splitting rows across servers is sharding, a different technique - replication keeps the same full data copied on every replica, not divided up.",
                        ),
                        choice(
                            "It rewrites slow queries automatically to run faster on the same server.",
                            false,
                            "Replication doesn't touch query logic or performance on a single server - it adds more copies of the whole database, it doesn't optimize queries.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "db-replication-lag",
                    prompt = "A user updates their email, then immediately reloads the page and still sees the old one. The read went to a replica. What's going on?",
                    choices = listOf(
                        choice(
                            "Replication lag - the write reached the primary, but hasn't propagated to that replica yet.",
                            true,
                            "Replicas apply changes shortly after the primary, not instantly - if the read happens in that gap, it sees the pre-update value, which is exactly the symptom described.",
                        ),
                        choice(
                            "The write was silently rejected because the new email address was already taken.",
                            false,
                            "A rejected write due to a conflict would typically produce an error response, not a value that quietly updates a moment later on its own.",
                        ),
                        choice(
                            "The CDN cached the page before the update happened.",
                            false,
                            "CDNs cache static assets, not live per-user account data pulled fresh from a database on each request - this scenario is a database-replication pattern.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "db-sharding",
            title = "Database Sharding",
            category = DATABASES,
            summary = "Sharding splits one large dataset across multiple database servers, each holding " +
                "only a slice of the rows (a shard), so no single server has to store or query the " +
                "entire dataset.",
            keyPoints = listOf(
                "Each shard holds a subset of rows, chosen by a shard key",
                "Scales total storage and write throughput beyond one machine",
                "A poor shard key can leave one shard much hotter than the others",
                "Queries that need rows from several shards become more expensive to run",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "sharding-vs-replication",
                    prompt = "A dataset has grown too large for any single server's disk to hold, even though read traffic is manageable. Which technique directly addresses that?",
                    choices = listOf(
                        choice(
                            "Sharding - splitting the rows across multiple servers so each one stores only part of the data.",
                            true,
                            "Replication copies the whole dataset onto every server, which doesn't help when the dataset itself is too big for one disk - sharding is what actually divides the storage burden.",
                        ),
                        choice(
                            "Replication - adding read replicas that each hold a full copy of the data.",
                            false,
                            "A full copy on every replica means every server still needs enough disk for the entire dataset - that doesn't solve a single-server storage limit.",
                        ),
                        choice(
                            "Caching - keeping the most popular rows in a fast in-memory store.",
                            false,
                            "A cache holds a small, popular subset for speed, but the full dataset still has to live somewhere durable - it doesn't solve the underlying storage-capacity problem.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "sharding-hot-shard",
                    prompt = "A social app shards user data by the first letter of the username. Traffic to usernames starting with 'A' is far higher than any other letter. What's the problem?",
                    choices = listOf(
                        choice(
                            "A poor shard key created a hot shard - one server takes disproportionate load while the rest sit underused.",
                            true,
                            "The shard key decides how evenly data and traffic spread out - a lopsided key like first-letter means one shard absorbs far more than its fair share, undoing the benefit of sharding.",
                        ),
                        choice(
                            "Sharding by letter is invalid and the database will refuse to start.",
                            false,
                            "Databases don't validate whether a shard key is well-balanced - sharding by letter runs fine mechanically, it's just a bad choice that creates uneven load.",
                        ),
                        choice(
                            "Every write would need to be copied to all the other shards for safety.",
                            false,
                            "Shards each own their own distinct data and don't need to mirror writes to each other - that cross-shard copying isn't part of how sharding works.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "message-queues",
            title = "Message Queues",
            category = SCALABILITY,
            summary = "A message queue lets one part of a system hand off work to another without " +
                "waiting for it to finish - the sender drops a message on the queue and moves on, " +
                "and a consumer processes it whenever it's ready.",
            keyPoints = listOf(
                "Decouples the producer of work from whoever processes it",
                "Smooths out traffic spikes by letting work queue up instead of failing",
                "Lets slow work (emails, video processing) happen off the main request path",
                "A slow or crashed consumer can be scaled up independently of the producer",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "queue-signup-email",
                    prompt = "Right after signup, an app sends a welcome email through a slow third-party provider, and users complain signup feels slow. What does putting the email behind a message queue fix?",
                    choices = listOf(
                        choice(
                            "The signup request can finish and respond as soon as the email job is queued, instead of waiting on the slow provider.",
                            true,
                            "Queuing the email turns 'send it right now, blocking the response' into 'hand it off and respond immediately' - a worker sends the email separately, off the user's critical path.",
                        ),
                        choice(
                            "It makes the third-party email provider itself respond faster.",
                            false,
                            "A queue doesn't change how fast the external provider is - it changes when in the request flow the app has to wait for that provider.",
                        ),
                        choice(
                            "It stores the new user's password more securely.",
                            false,
                            "Password storage is a hashing-and-security concern, completely unrelated to whether the welcome email is sent synchronously or through a queue.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "queue-traffic-spike",
                    prompt = "A video-processing service gets a sudden burst of ten times its normal upload volume. Processing jobs go through a queue. What happens?",
                    choices = listOf(
                        choice(
                            "Jobs pile up in the queue and get processed as workers become free, instead of the burst overwhelming the service directly.",
                            true,
                            "The queue acts as a buffer - it absorbs the spike so uploads still succeed, and the backlog just drains a bit slower until workers catch up, rather than the service falling over.",
                        ),
                        choice(
                            "The queue immediately drops any job beyond ten times normal volume.",
                            false,
                            "A queue doesn't have a built-in multiplier limit like that - it keeps accepting messages up to its own configured capacity, not a fixed multiple of 'normal' traffic.",
                        ),
                        choice(
                            "Every worker consuming the queue crashes at the same time.",
                            false,
                            "A traffic spike arriving through a queue doesn't inherently crash consumers - the queue's whole purpose is to prevent that kind of overload from hitting workers all at once.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "rate-limiting",
            title = "Rate Limiting",
            category = APIS,
            summary = "Rate limiting caps how many requests a client can make in a given time window, " +
                "protecting the service from being overwhelmed by one caller - whether that caller " +
                "is buggy, abusive, or just very active.",
            keyPoints = listOf(
                "Caps requests per client over a time window (e.g. 100 per minute)",
                "Protects shared infrastructure from any single caller overwhelming it",
                "Exceeding the limit typically returns an HTTP 429 with a retry-after hint",
                "Different endpoints or client tiers can have different limits",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "rate-limit-purpose",
                    prompt = "A public API has no rate limiting, and one client's misconfigured script sends thousands of requests per second. What's the direct consequence?",
                    choices = listOf(
                        choice(
                            "That one client can degrade or take down the service for every other client sharing the same infrastructure.",
                            true,
                            "Without a cap, one caller's traffic isn't isolated from anyone else's - shared servers, database connections, and bandwidth all get consumed by the one misbehaving client.",
                        ),
                        choice(
                            "The API's data would become permanently inconsistent across replicas.",
                            false,
                            "Replication consistency is governed by how writes propagate, not by how many requests one client sends - a read-heavy flood wouldn't corrupt replica consistency on its own.",
                        ),
                        choice(
                            "The client's own requests would automatically start failing with a 500 error.",
                            false,
                            "Without rate limiting in place, nothing specifically causes the offending client's own requests to fail - if anything, everyone's requests (including theirs) suffer together.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "rate-limit-response",
                    prompt = "A client exceeds its allotted requests per minute. What should a well-designed API do?",
                    choices = listOf(
                        choice(
                            "Reject the extra requests with a 429 status and tell the client when it can retry.",
                            true,
                            "A 429 with a retry hint is the standard, well-behaved way to signal 'you're over your limit' - it lets the client back off and try again without guessing.",
                        ),
                        choice(
                            "Silently process the requests anyway but skip validating their input.",
                            false,
                            "Skipping validation doesn't reduce load and introduces a correctness risk - the point of a limit is to reduce the number of requests handled, not to process them more carelessly.",
                        ),
                        choice(
                            "Permanently ban the client's account after the very first time it happens.",
                            false,
                            "A permanent ban on the first overage is far harsher than necessary - rate limiting is meant to smooth out bursts, not treat every burst as grounds for a permanent block.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "cap-theorem",
            title = "Consistency, Availability, Partition Tolerance",
            category = THEORY,
            summary = "The CAP theorem says a distributed system can't fully guarantee Consistency " +
                "(every read sees the latest write) and Availability (every request gets a response) " +
                "at the same time once the network partitions - it has to give up one of them.",
            keyPoints = listOf(
                "Network partitions will happen, so this tradeoff is unavoidable at scale",
                "Choosing consistency (CP) means some requests fail or wait during a partition",
                "Choosing availability (AP) means some requests may return stale data during a partition",
                "The tradeoff only bites during an actual partition - most of the time both hold",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "cap-tradeoff",
                    prompt = "During a network partition, a distributed database can either answer every request (possibly with stale data) or refuse some requests to guarantee freshness. What does CAP say about having both, fully, at once?",
                    choices = listOf(
                        choice(
                            "It's not possible - a partition forces a choice between staying fully consistent and staying fully available.",
                            true,
                            "That's the core claim of CAP: consistency and availability can both hold when the network is fine, but the moment nodes can't talk to each other, you must sacrifice one to keep the other.",
                        ),
                        choice(
                            "It's possible as long as the database uses SSDs instead of spinning disks.",
                            false,
                            "Storage hardware speed has nothing to do with this tradeoff - CAP is about what happens when nodes can't communicate, not how fast a single node reads or writes.",
                        ),
                        choice(
                            "It's possible as long as there is only one database server.",
                            false,
                            "CAP is specifically about distributed systems with multiple nodes - a single server can't experience a network partition between nodes because there's only one node.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "cap-bank-vs-social",
                    prompt = "Between a bank's account balance and a social feed's like count, which more naturally leans toward choosing consistency over availability during a partition?",
                    choices = listOf(
                        choice(
                            "The bank balance - showing a stale or wrong number has real financial consequences.",
                            true,
                            "Money is a case where being wrong is worse than being briefly unavailable - a bank would rather delay or reject a request than risk showing (or acting on) an incorrect balance.",
                        ),
                        choice(
                            "The like count - a slightly wrong number is far more damaging to users than a wrong bank balance.",
                            false,
                            "It's the reverse - a like count being off by a few for a moment barely matters to anyone, while a wrong account balance can mean real financial harm.",
                        ),
                        choice(
                            "Neither - CAP tradeoffs only apply to systems that don't use any database at all.",
                            false,
                            "CAP applies to any distributed data store, databases very much included - this claim inverts what CAP is actually about.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "rest-vs-graphql",
            title = "REST vs GraphQL",
            category = APIS,
            summary = "REST exposes a fixed set of endpoints that each return a predetermined shape of " +
                "data, while GraphQL exposes a single endpoint where the client describes exactly " +
                "which fields it wants back in one request.",
            keyPoints = listOf(
                "REST: many endpoints, each with a fixed response shape",
                "GraphQL: one endpoint, client specifies the exact fields it needs",
                "GraphQL avoids over-fetching and under-fetching by letting the client ask precisely",
                "REST responses are easy to cache by URL; GraphQL needs its own caching strategy",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "restvsgraphql-mobile",
                    prompt = "A mobile screen needs a user's name and avatar, but the REST endpoint for a user returns twenty fields including ones the screen never uses. What does GraphQL change here?",
                    choices = listOf(
                        choice(
                            "The client can request just the name and avatar fields, so the response carries only what's needed.",
                            true,
                            "GraphQL lets the client specify exactly which fields it wants in the query, so the server returns only those - avoiding the over-fetching that a fixed REST response shape causes.",
                        ),
                        choice(
                            "The server automatically deletes the twenty unused fields from its database.",
                            false,
                            "GraphQL changes what a single response includes, not what data exists in storage - the other seventeen fields still exist for whichever request actually needs them.",
                        ),
                        choice(
                            "The request would need to hit twenty separate endpoints instead of one.",
                            false,
                            "That's the opposite of how GraphQL works - it consolidates a request into one endpoint and one query, rather than multiplying the number of endpoints called.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "restvsgraphql-caching",
                    prompt = "A team relies heavily on caching responses by URL at the CDN layer. Which API style fits that caching approach more naturally?",
                    choices = listOf(
                        choice(
                            "REST - each distinct URL maps to one predictable response, which a CDN can cache by that URL.",
                            true,
                            "Because a REST endpoint's URL determines its response shape, a CDN can cache 'GET /users/5' as one stable entry - GraphQL's single endpoint with varying queries doesn't map onto URL-based caching the same way.",
                        ),
                        choice(
                            "GraphQL - every query goes to the same URL, which makes URL-based caching more effective.",
                            false,
                            "The opposite is true - because every GraphQL query hits the same endpoint URL regardless of what's being asked, a plain URL-based cache can't distinguish one query's response from another's.",
                        ),
                        choice(
                            "Neither style produces responses that any caching layer could ever store.",
                            false,
                            "Both styles can be cached with the right strategy - REST just happens to fit simple URL-based caching more directly out of the box.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "idempotency",
            title = "Idempotency",
            category = APIS,
            summary = "An idempotent operation has the same effect no matter how many times it's " +
                "applied. That's what makes 'just retry on failure' a safe default instead of a " +
                "risk - a client can resend a request after a timeout without worrying it will " +
                "happen twice.",
            keyPoints = listOf(
                "Repeating the same request has the same effect as sending it once",
                "GET, PUT, and DELETE are idempotent by convention; plain POST is not",
                "A client-generated idempotency key lets the server recognize a retried POST",
                "Idempotency is what makes automatic retries on timeout safe to build in",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "idempotency-payment-retry",
                    prompt = "A payment request times out after the charge actually succeeded on the server, so the client retries the same request. What stops the customer from being charged twice?",
                    choices = listOf(
                        choice(
                            "An idempotency key sent with the request lets the server recognize the retry and return the original result instead of charging again.",
                            true,
                            "The key ties both attempts to the same logical operation, so the server can look it up, see the charge already happened, and hand back that result rather than repeating the side effect.",
                        ),
                        choice(
                            "The client waits ten seconds before retrying, which is enough time for the first charge to fully complete.",
                            false,
                            "A delay changes timing, not correctness - without a way to recognize the retry as the same operation, waiting longer doesn't prevent the second attempt from also charging the customer.",
                        ),
                        choice(
                            "HTTP itself automatically discards any two identical requests sent within a minute.",
                            false,
                            "HTTP has no built-in request deduplication - preventing a duplicate charge is something the API has to implement deliberately, not something the protocol does for free.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "idempotency-methods",
                    prompt = "Which of these is idempotent by convention - repeating it has the same effect as doing it once?",
                    choices = listOf(
                        choice(
                            "PUT /users/5 with the same body - the user ends up in the same state whether it's sent once or five times.",
                            true,
                            "PUT means 'set this resource to exactly this state,' so sending the identical update again just sets it to the same state again - nothing changes on the second, third, or later attempt.",
                        ),
                        choice(
                            "POST /orders with the same body - a plain create endpoint that adds a new record on every call.",
                            false,
                            "A bare create endpoint typically makes a new order each time it's called, so calling it twice produces two orders - that's the opposite of idempotent unless an idempotency key is added.",
                        ),
                        choice(
                            "Any request at all, as long as it uses HTTPS instead of plain HTTP.",
                            false,
                            "Encryption in transit is a security property and has no bearing on whether repeating a request changes its effect - idempotency depends on what the operation does, not on the transport.",
                        ),
                    ),
                ),
            ),
        ),
        SystemDesignConcept(
            id = "webhooks-vs-polling",
            title = "Webhooks vs Polling",
            category = APIS,
            summary = "Polling means the client repeatedly asks 'has anything changed yet?' on a " +
                "schedule. A webhook flips that around: the server calls the client back the " +
                "instant something actually happens, so there's no need to keep asking.",
            keyPoints = listOf(
                "Polling: the client checks on a schedule, whether or not anything changed",
                "Webhook: the server pushes a notification the moment an event occurs",
                "Webhooks cut wasted requests, but need the receiver to expose a reachable endpoint",
                "Polling is simpler to set up and still works if the receiver is offline sometimes",
            ),
            questions = listOf(
                SystemDesignQuestion(
                    id = "webhook-vs-poll-tradeoff",
                    prompt = "A shipping partner's status rarely changes, but an app checks its API every 10 seconds anyway just in case. What does switching to a webhook fix?",
                    choices = listOf(
                        choice(
                            "Almost all of those checks were wasted - a webhook only fires when the status actually changes.",
                            true,
                            "Polling pays the cost of asking on a fixed schedule regardless of whether there's news, while a webhook only sends a request when there's genuinely something to report, cutting out nearly all the empty checks.",
                        ),
                        choice(
                            "The shipping partner's own servers would process orders faster internally.",
                            false,
                            "How the app is notified of a status change doesn't affect the partner's internal order processing speed - that's a separate system entirely from the notification mechanism.",
                        ),
                        choice(
                            "The app would no longer need any network connection at all.",
                            false,
                            "A webhook still requires the app to be reachable over the network to receive the callback - it changes who initiates the request, not whether a network is involved.",
                        ),
                    ),
                ),
                SystemDesignQuestion(
                    id = "webhook-vs-poll-when-poll-wins",
                    prompt = "A mobile app needs updates, but the phone is frequently offline or behind a firewall that blocks incoming connections. Which approach still works reliably?",
                    choices = listOf(
                        choice(
                            "Polling - the client initiates every check, so it just tries again next time it's online, with nothing needing to reach it directly.",
                            true,
                            "Since polling only relies on the client being able to make outgoing requests when it happens to be online, it sidesteps the problem entirely - there's no inbound callback that could be blocked or missed.",
                        ),
                        choice(
                            "Webhooks - the server will simply retry delivering the callback forever until the phone comes back online.",
                            false,
                            "A webhook needs to reach the receiver directly, and a firewall blocking incoming connections (or a phone that's offline) means those callbacks can be missed or undeliverable, not reliably queued forever.",
                        ),
                        choice(
                            "Neither approach can work if a device is ever offline for any period of time.",
                            false,
                            "Polling specifically tolerates intermittent connectivity just fine - the client simply catches up by asking again the next time it's online, so this claim overstates the limitation.",
                        ),
                    ),
                ),
            ),
        ),
    )

    fun byId(id: String): SystemDesignConcept? = concepts.firstOrNull { it.id == id }

    fun byCategory(category: SystemDesignCategory): List<SystemDesignConcept> =
        concepts.filter { it.category == category }

    val allQuestions: List<SystemDesignQuestion> get() = concepts.flatMap { it.questions }

    /** A stable daily pick, so everyone sees the same question on a given day. */
    fun questionForDay(epochDay: Long): SystemDesignQuestion {
        val all = allQuestions
        val index = ((epochDay % all.size) + all.size) % all.size
        return all[index.toInt()]
    }

    fun conceptFor(question: SystemDesignQuestion): SystemDesignConcept =
        concepts.first { concept -> concept.questions.any { it.id == question.id } }
}
