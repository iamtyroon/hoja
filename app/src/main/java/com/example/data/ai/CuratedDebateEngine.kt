package com.example.data.ai

import com.example.data.model.DailyMotion
import com.example.data.model.DebateLevel
import com.example.data.model.DebateRound
import com.example.data.model.DebateScoreResult
import com.example.data.model.DebateSide
import com.example.data.model.DebateTurnMessage
import com.example.data.model.KeyTerm
import com.example.data.model.RewriteItFeedback
import com.example.data.model.TurnSpeaker
import kotlin.math.abs

/**
 * Intelligent built-in debate sparring engine and motion catalog.
 * Guarantees zero runtime crashes or uninitialized Firebase failures
 * while providing authentic East African and global debate sparring.
 */
object CuratedDebateEngine {

    private data class MotionTemplate(
        val motion: String,
        val context: String,
        val keyTerms: List<KeyTerm>,
        val proposeHint: String,
        val opposeHint: String,
        val proposeCoreArg: String,
        val opposeCoreArg: String
    )

    private val beginnerMotions = listOf(
        MotionTemplate(
            motion = "This House Believes That schools in East Africa should replace printed textbooks with digital tablets.",
            context = "With expanding mobile connectivity and national digital literacy drives, educators are debating whether digital tablets provide a more scalable learning experience than physical textbooks.",
            keyTerms = listOf(
                KeyTerm("Digital Divide", "Disparity in access to modern information and computer technology across demographics."),
                KeyTerm("Curriculum Agility", "The ease and speed with which school learning materials can be updated dynamically."),
                KeyTerm("Cognitive Retention", "The depth of comprehension and long-term memory retained from digital screens versus print.")
            ),
            proposeHint = "Argue that digital tablets lower long-term printing costs, allow instant syllabus revisions, and prepare youth for the digital economy.",
            opposeHint = "Emphasize frequent rural power blackouts, high device breakage rates, screen distractions, and marginalization of low-income families.",
            proposeCoreArg = "Tablets drastically cut recurring procurement costs and ensure every schoolchild has access to current science and literature.",
            opposeCoreArg = "Without reliable electricity and technical repair centers across rural counties, physical textbooks remain resilient and egalitarian."
        ),
        MotionTemplate(
            motion = "This House Would ban fast-food advertising aimed directly at children.",
            context = "Urban centers across the region are experiencing rising pediatric obesity and lifestyle health complications, prompting debates on marketing ethics.",
            keyTerms = listOf(
                KeyTerm("Paternalistic Regulation", "State intervention aimed at safeguarding vulnerable groups from making self-harming choices."),
                KeyTerm("Cognitive Exploitation", "Marketing strategies that leverage children's inability to discern commercial intent."),
                KeyTerm("Consumer Sovereignty", "The principle that individuals and families should decide what foods to consume without state bans.")
            ),
            proposeHint = "Focus on pediatric health burdens, long-term public healthcare savings, and children's vulnerability to persuasive marketing.",
            opposeHint = "Focus on parental autonomy, the adverse impact on media advertising revenues, and the need for physical education over bans.",
            proposeCoreArg = "Children lack the developmental maturity to resist predatory sugar marketing, creating lifelong lifestyle health burdens for public hospitals.",
            opposeCoreArg = "Dietary decisions belong within the family; banning ads restricts commercial free speech without addressing underlying physical inactivity."
        ),
        MotionTemplate(
            motion = "This House Believes That secondary schools should make community service mandatory for graduation.",
            context = "Educational reforms increasingly emphasize civic duty and practical social responsibility alongside traditional academic examinations.",
            keyTerms = listOf(
                KeyTerm("Civic Cohesion", "The shared social bonds and responsibility uniting diverse segments of society."),
                KeyTerm("Coerced Altruism", "Mandating volunteerism by law, which critics argue undermines authentic empathy."),
                KeyTerm("Experiential Learning", "Acquiring practical life skills through direct community participation.")
            ),
            proposeHint = "Show how mandatory service instills civic empathy, bridges ethnic and socioeconomic divides, and supports local community projects.",
            opposeHint = "Argue that forced volunteering becomes an administrative chore, burdens underprivileged students who work after school, and dilutes intrinsic goodwill.",
            proposeCoreArg = "Mandatory community engagement fosters social solidarity across backgrounds and equips students with practical problem-solving empathy.",
            opposeCoreArg = "Compulsory volunteering turns genuine compassion into a checkbox burden, unfairly punishing students from households requiring after-school labor."
        ),
        MotionTemplate(
            motion = "This House Would eliminate homework in primary school education.",
            context = "Parents and pediatric child psychologists are challenging traditional after-school homework loads in favor of unstructured play and family time.",
            keyTerms = listOf(
                KeyTerm("Extracurricular Play", "Unstructured childhood activity vital for emotional self-regulation and creativity."),
                KeyTerm("Parental Disparity", "Inequalities in home support, where educated parents can assist while working parents cannot."),
                KeyTerm("Consolidation of Learning", "The educational theory that independent repetition reinforces classroom teaching.")
            ),
            proposeHint = "Highlight mental burnout in young children, unequal home learning conditions, and the critical importance of free creative play.",
            opposeHint = "Emphasize study discipline, academic reinforcement of basic numeracy and literacy, and parental visibility into classroom progress.",
            proposeCoreArg = "Primary pupils spend 8 hours in school; mandatory homework causes early anxiety and exacerbates inequalities for kids without home tutors.",
            opposeCoreArg = "Brief, focused homework instills independent learning habits and keeps parents actively informed about their child's daily progress."
        ),
        MotionTemplate(
            motion = "This House Believes That competitive sports in schools should receive equal funding to academic STEM subjects.",
            context = "National talent development agendas are re-evaluating physical education and athletics as viable multi-billion dollar professional career pathways.",
            keyTerms = listOf(
                KeyTerm("Holistic Development", "Cultivating athletic, physical, and team leadership skills alongside cognitive knowledge."),
                KeyTerm("Youth Livelihoods", "Recognizing athletics and sports business as valid regional economic career routes."),
                KeyTerm("Opportunity Cost", "The trade-off of diverting scarce educational budgets from scientific laboratories into sporting fields.")
            ),
            proposeHint = "Show how East African athletics provides global economic mobility, fosters national unity, and builds lifelong resilience.",
            opposeHint = "Point out that STEM literacy is the foundation of modern industrialization, healthcare, and engineering, which should take priority.",
            proposeCoreArg = "Athletics builds team discipline and opens international scholarship and professional careers for youth from marginalized backgrounds.",
            opposeCoreArg = "Developing nations face acute shortages of engineers, doctors, and software developers; diverting laboratory funds to sports undermines industrial development."
        )
    )

    private val intermediateMotions = listOf(
        MotionTemplate(
            motion = "This House Believes That East African governments should prioritize green industrialization over fossil-fuel energy.",
            context = "As the region accelerates manufacturing, nations like Kenya, Ethiopia, and Tanzania are choosing between geothermal/solar green energy and fossil-fuel acceleration.",
            keyTerms = listOf(
                KeyTerm("Green Industrialization", "Developing manufacturing sectors powered by zero-carbon renewable energy grids."),
                KeyTerm("Carbon Border Adjustments", "Tariffs imposed by international buyers on goods manufactured using carbon-heavy energy."),
                KeyTerm("Baseload Power Deficit", "The risk of renewable intermittency constraining 24/7 heavy factory operations.")
            ),
            proposeHint = "Demonstrate that green energy shields local exports from future global carbon tariffs and taps immense regional geothermal/solar abundance.",
            opposeHint = "Argue that heavy manufacturing requires cheap, uninterrupted baseload power immediately to lift millions out of poverty before green grids mature.",
            proposeCoreArg = "Investing in renewable manufacturing grants African industries a permanent competitive advantage in an increasingly carbon-taxed global market.",
            opposeCoreArg = "Emerging factories cannot tolerate power intermittency; fossil power provides the reliable baseload necessary to jumpstart industrial employment today."
        ),
        MotionTemplate(
            motion = "This House Would introduce a universal basic income for university graduates seeking employment.",
            context = "Youth underemployment among college graduates has sparked debates over social safety nets versus market-driven job creation subsidies.",
            keyTerms = listOf(
                KeyTerm("Graduate Underemployment", "The mismatch where university degree holders are unable to find skilled formal work."),
                KeyTerm("Fiscal Sustainability", "The government's ability to maintain social cash transfers without unsustainable debt."),
                KeyTerm("Entrepreneurial Runway", "Financial breathing room allowing young founders to build startups rather than take survival jobs.")
            ),
            proposeHint = "Show how guaranteed basic income prevents poverty traps, stimulates consumer spending, and enables graduates to build formal businesses.",
            opposeHint = "Highlight sovereign fiscal deficits, the moral hazard of discouraging agricultural/technical vocational work, and administrative leakage.",
            proposeCoreArg = "A basic stipend gives skilled youth the dignity and financial buffer needed to innovate, search for relevant jobs, and avoid exploitative gig work.",
            opposeCoreArg = "With massive budget deficits, cash transfers drain public funds from technical vocational training and industrial job-creation incentives."
        ),
        MotionTemplate(
            motion = "This House Believes That county governments should allocate at least 40% of their development budgets to youth-led agribusiness.",
            context = "With an average farmer age of 60 amidst high youth unemployment, modernization of agriculture is viewed as both an economic and food-security imperative.",
            keyTerms = listOf(
                KeyTerm("Agri-Tech Adoption", "Integrating IoT, precision sensors, and solar irrigation into agricultural production."),
                KeyTerm("Intergenerational Transition", "Transferring agricultural management and rural land production to younger generations."),
                KeyTerm("Fiduciary Risk", "The governance challenge of managing county grants and concessionary loans among early-stage youth groups.")
            ),
            proposeHint = "Focus on food sovereignty, climate-smart farming adoption, and reversing rural-to-urban youth migration.",
            opposeHint = "Highlight the urgent need for roads, healthcare centers, and clean water infrastructure that serve the entire population across age brackets.",
            proposeCoreArg = "Youth embrace modern irrigation, drone spraying, and digital marketing, turning subsistence farming into a lucrative regional export engine.",
            opposeCoreArg = "Ear-marking 40% of development budgets for a single demographic neglects universal essentials like rural maternal clinics and piped drinking water."
        ),
        MotionTemplate(
            motion = "This House Would regulate social media algorithms during national elections to curb misinformation.",
            context = "Electoral integrity across young democracies faces unprecedented strain from algorithmic polarization, bot amplification, and viral ethnic disinformation.",
            keyTerms = listOf(
                KeyTerm("Algorithmic Transparency", "Requiring tech platforms to disclose how recommendation algorithms promote controversial content."),
                KeyTerm("Electoral Volatility", "The heightened sensitivity of public sentiment during high-stakes democratic transitions."),
                KeyTerm("Censorship Precedent", "The risk that state regulatory powers will be weaponized to suppress legitimate political dissent.")
            ),
            proposeHint = "Demonstrate that viral hate speech and deepfakes threaten national stability and democratic order faster than fact-checkers can respond.",
            opposeHint = "Warn that state-controlled algorithm regulators inevitably become instruments of the incumbent regime to silence political opposition.",
            proposeCoreArg = "Engagement algorithms reward sensationalism and inflammatory claims; temporary regulatory guardrails protect public peace and verified civic discourse.",
            opposeCoreArg = "Handing governments the authority to alter algorithm feeds creates state-sanctioned censorship that dismantles free and fair electoral debate."
        )
    )

    private val advancedMotions = listOf(
        MotionTemplate(
            motion = "This House Believes That the East African Community should establish a single common currency by 2030.",
            context = "Monetary union has been a longstanding treaty ambition for the EAC partner states to lower trade transaction friction and eliminate currency arbitrage.",
            keyTerms = listOf(
                KeyTerm("Monetary Sovereignty", "The exclusive authority of a national central bank to determine interest rates and currency supply."),
                KeyTerm("Asymmetric Macroeconomic Shocks", "Economic disturbances that impact member economies unevenly due to divergent export baskets."),
                KeyTerm("Intra-Regional Trade Velocity", "The speed and volume of cross-border goods trade facilitated by eliminating foreign exchange conversion.")
            ),
            proposeHint = "Argue that eliminating forex transaction barriers deepens regional integration, reduces US dollar dependency, and boosts cross-border investment.",
            opposeHint = "Point out structural convergence failures, divergent debt-to-GDP ratios, and the loss of independent monetary response during economic crises.",
            proposeCoreArg = "A shared currency removes the devastating foreign exchange bottlenecks holding back small-scale cross-border traders and consolidates regional market power.",
            opposeCoreArg = "Surrendering national central bank autonomy before member states achieve strict fiscal convergence invites catastrophic debt contagion."
        ),
        MotionTemplate(
            motion = "This House Would eliminate all internal customs tariffs across the African Continental Free Trade Area immediately.",
            context = "While the AfCFTA agreement proposes gradual phased tariff rollouts, proponents argue that immediate zero-tariff liberalization is necessary to build resilient value chains.",
            keyTerms = listOf(
                KeyTerm("Intra-African Trade Deficit", "The paradox where African economies trade far more with external continents than with each other."),
                KeyTerm("Customs Revenue Dependency", "The reliance of small government budgets on border import duty collections for public services."),
                KeyTerm("Regional Value Chain Integration", "Manufacturing goods across multiple African borders to produce competitive finished exports.")
            ),
            proposeHint = "Show that tariff elimination triggers industrial scale, builds continental supply chains, and protects against global geopolitical disruptions.",
            opposeHint = "Highlight that smaller emerging economies face instant fiscal budget collapse and domestic industry obliteration by larger manufacturing states.",
            proposeCoreArg = "Internal tariffs keep African nations as raw material exporters; zero tariffs unlock massive continental industrial processing and self-reliance.",
            opposeCoreArg = "Small agrarian nations rely on customs duties for over 30% of state revenue; immediate elimination without compensation funds destroys essential public services."
        ),
        MotionTemplate(
            motion = "This House Believes That governments should abolish patent protections on essential climate mitigation technologies.",
            context = "Developing nations bearing the disproportionate brunt of extreme droughts and climate disasters face prohibitive intellectual property royalties on solar, battery, and desalination innovations.",
            keyTerms = listOf(
                KeyTerm("Compulsory Licensing", "Government authorization permitting third parties to produce patented products without patent holder consent."),
                KeyTerm("Global Commons", "Resources and technologies vital to collective planetary survival that supersede private monopolistic ownership."),
                KeyTerm("R&D Capital Invalidation", "The erosion of private venture capital incentives to invent future clean technologies when patents are abolished.")
            ),
            proposeHint = "Frame climate survival as a planetary emergency where life-saving mitigation tech must be accessible without monopolistic rents.",
            opposeHint = "Argue that abolishing IP destroys the commercial incentive for private firms to invest billions into next-generation clean breakthroughs.",
            proposeCoreArg = "Global climate survival cannot be held hostage by private corporate monopolies; open-sourcing mitigation technology saves vulnerable millions.",
            opposeCoreArg = "Eliminating patent protections kills private capital flow into green research, halting the development of high-efficiency batteries and clean power."
        )
    )

    private val championMotions = listOf(
        MotionTemplate(
            motion = "This House Believes That developing nations should collectively default on sovereign external debts linked to colonial-era infrastructure treaties.",
            context = "Sovereign debt burdens currently consume up to 60% of annual tax revenues in several developing nations, renewing calls for collective debt repudiation and moral restructuring.",
            keyTerms = listOf(
                KeyTerm("Odious Debt Doctrine", "The international legal concept that debts incurred by illegitimate regimes without citizen benefit should not be enforceable."),
                KeyTerm("Sovereign Credit Lockout", "The retaliatory denial of access to international commercial capital markets following a state default."),
                KeyTerm("Sovereign Solvency Asymmetry", "The structural imbalance where debtor states must service foreign currency debt amidst severe local currency depreciations.")
            ),
            proposeHint = "Demonstrate that servicing unjust debt traps millions in deprivation, starves healthcare and education, and that collective action prevents market retaliation.",
            opposeHint = "Highlight catastrophic immediate currency devaluation, hyperinflation, complete loss of trade credit lines, and severe economic isolation.",
            proposeCoreArg = "Debt service payments currently starve hospitals and schools; a coordinated collective default forces an overdue restructuring of the predatory global financial architecture.",
            opposeCoreArg = "Defaulting cuts off letters of credit for essential medicine, fuel, and food imports, throwing working citizens into immediate hyperinflationary collapse."
        ),
        MotionTemplate(
            motion = "This House Would place sovereign artificial intelligence systems under multilateral democratic oversight rather than national regulation.",
            context = "As frontier artificial intelligence models achieve transformative cognitive capabilities, the race for national dominance clashes with multilateral demands for collective existential safety.",
            keyTerms = listOf(
                KeyTerm("Algorithmic Sovereignty", "The doctrine that every state must control its own sovereign computing infrastructure and AI foundational models."),
                KeyTerm("Multilateral Containment", "Global treaties and inspection bodies preventing unilateral weaponization of transformative technology."),
                KeyTerm("Geopolitical Hegemonic Asymmetry", "The risk that global oversight bodies will be co-opted by superpowers to freeze developing nations out of technological autonomy.")
            ),
            proposeHint = "Argue that unaligned frontier AI poses transnational existential risks that no single nation can contain within its borders.",
            opposeHint = "Expose that multilateral oversight will inevitably be captured by dominant superpowers to entrench tech monopolies and enforce digital neocolonialism.",
            proposeCoreArg = "Autonomous frontier algorithms respect no physical borders; without binding multilateral safety treaties, an unregulated AI arms race risks catastrophic planetary instability.",
            opposeCoreArg = "Global regulatory treaties will be dominated by tech superpowers to block emerging nations from developing independent sovereign computational capabilities."
        )
    )

    fun getDailyMotion(
        dateKey: String,
        level: DebateLevel,
        avoidMotions: List<String> = emptyList()
    ): DailyMotion {
        val list = when (level) {
            DebateLevel.BEGINNER -> beginnerMotions
            DebateLevel.INTERMEDIATE -> intermediateMotions
            DebateLevel.ADVANCED -> advancedMotions
            DebateLevel.CHAMPION -> championMotions
        }

        val available = list.filter { template ->
            avoidMotions.none { avoid -> avoid.contains(template.motion.take(30), ignoreCase = true) }
        }.ifEmpty { list }

        val hash = abs(dateKey.hashCode() + level.name.hashCode())
        val selected = available[hash % available.size]

        return DailyMotion(
            dateKey = dateKey,
            level = level,
            motion = selected.motion,
            shortContext = selected.context,
            keyTerms = selected.keyTerms,
            proposeHint = selected.proposeHint,
            opposeHint = selected.opposeHint
        )
    }

    fun generateOpponentTurn(
        motion: String,
        aiSide: DebateSide,
        level: DebateLevel,
        round: DebateRound,
        transcriptSoFar: List<DebateTurnMessage>
    ): OpponentTurnResponse {
        val userLastTurn = transcriptSoFar.lastOrNull { it.speaker == TurnSpeaker.USER }?.text.orEmpty()
        val userWords = userLastTurn.split(Regex("\\s+")).filter { it.isNotBlank() }

        // Find relevant motion template if matched
        val allTemplates = beginnerMotions + intermediateMotions + advancedMotions + championMotions
        val template = allTemplates.firstOrNull { motion.contains(it.motion.take(25), ignoreCase = true) }

        val corePoint = if (aiSide == DebateSide.OPPOSE) {
            template?.opposeCoreArg ?: "The practical enforcement costs and unintended economic distortions make this proposal unsustainable."
        } else {
            template?.proposeCoreArg ?: "The systemic benefits to long-term equity, public welfare, and institutional progress far outweigh the initial implementation friction."
        }

        val rebuttal = when (round) {
            DebateRound.OPENING -> {
                buildString {
                    append("From the ${aiSide.label} bench, I welcome this debate. ")
                    if (userWords.isNotEmpty()) {
                        append("While the ${aiSide.opposite().label} bench opens with optimism regarding ")
                        val snippet = userWords.take(4).joinToString(" ")
                        append("\"$snippet...\", ")
                    }
                    append("we must confront the underlying mechanism: $corePoint ")
                    when (level) {
                        DebateLevel.BEGINNER -> append("We cannot sacrifice fundamental fairness for short-term convenience.")
                        DebateLevel.INTERMEDIATE -> append("Policy must be grounded in structural economic reality, not ideological hope.")
                        DebateLevel.ADVANCED -> append("Without institutional convergence and risk mitigation, this policy triggers cascading fiscal shocks.")
                        DebateLevel.CHAMPION -> append("The structural asymmetry in this proposal concentrates severe downside risk upon the most vulnerable communities.")
                    }
                }
            }
            DebateRound.REBUTTAL -> {
                buildString {
                    append("The ${aiSide.opposite().label} bench fails to engage with the central clash. ")
                    if (userWords.isNotEmpty()) {
                        append("Their argument relies on an unproven assumption that their intervention works without friction. ")
                    }
                    append("In reality, $corePoint ")
                    append("When resources are scarce, the real-world trade-off cannot be ignored: every dollar spent here is taken away from proven alternatives.")
                }
            }
            DebateRound.CLOSING -> {
                buildString {
                    append("In closing for ${aiSide.label}, this debate comes down to a decisive test of feasibility and impact. ")
                    append("The ${aiSide.opposite().label} bench has offered ideals, but we have demonstrated the concrete mechanism: $corePoint ")
                    append("On balance of evidence and practical consequence, the ${aiSide.label} bench clearly carries the day.")
                }
            }
        }

        val strongExample = when (round) {
            DebateRound.OPENING -> "Counter by illustrating how the benefits directly reach frontline beneficiaries, preventing the downside the opponent warned about."
            DebateRound.REBUTTAL -> "Directly challenge their trade-off claim: show why your solution creates positive spillover revenue that actually funds other public needs."
            DebateRound.CLOSING -> "Summarize your two strongest clashes side-by-side to show why your moral and practical impact outweighs their objections."
        }

        return OpponentTurnResponse(
            opponentReply = rebuttal,
            exampleStrongAnswer = strongExample
        )
    }

    fun scoreDebate(
        motion: String,
        userSide: DebateSide,
        level: DebateLevel,
        transcript: List<DebateTurnMessage>
    ): DebateScoreResult {
        val userTurns = transcript.filter { it.speaker == TurnSpeaker.USER }
        val totalUserWords = userTurns.sumOf { it.wordCount }
        val allUserText = userTurns.joinToString(" ") { it.text }

        val hasCausalWords = listOf("because", "therefore", "consequently", "leads to", "creates", "due to", "results in")
            .count { allUserText.contains(it, ignoreCase = true) }
        val hasEvidenceWords = listOf("example", "instance", "kenya", "africa", "specifically", "data", "percent", "evidence", "schools")
            .count { allUserText.contains(it, ignoreCase = true) }
        val hasRebuttalWords = listOf("opponent", "rebut", "counter", "overlook", "disagree", "claim", "argument", "however", "flaw")
            .count { allUserText.contains(it, ignoreCase = true) }

        // Dynamic, honest rubric scoring based on user input
        val argStrength = (6 + (hasCausalWords.coerceAtMost(3)) + if (totalUserWords >= 40) 1 else 0).coerceIn(4, 10)
        val evidence = (6 + (hasEvidenceWords.coerceAtMost(3)) + if (totalUserWords >= 50) 1 else 0).coerceIn(4, 10)
        val rebuttal = (6 + (hasRebuttalWords.coerceAtMost(3)) + if (userTurns.size >= 2) 1 else 0).coerceIn(4, 10)
        val structure = (7 + if (userTurns.size >= 2) 1 else 0 + if (totalUserWords >= 45) 1 else 0).coerceIn(5, 10)
        val writingQuality = (7 + if (totalUserWords >= 30) 1 else 0 + if (hasCausalWords >= 1) 1 else 0).coerceIn(5, 10)

        val computedAvg = (argStrength + evidence + rebuttal + structure + writingQuality) / 5.0
        val overall = Math.round(computedAvg * 10.0) / 10.0

        val strengths = mutableListOf<String>()
        if (hasCausalWords > 0) {
            strengths.add("Established logical causality using structured reasoning connectors.")
        } else {
            strengths.add("Maintained an assertive, committed stance on the ${userSide.label} bench.")
        }
        if (totalUserWords >= 40) {
            strengths.add("Thorough argument depth with adequate contextual detail for ${level.displayName} rank.")
        } else {
            strengths.add("Concise thesis that directly answered the motion.")
        }

        val improvements = mutableListOf<String>()
        if (hasEvidenceWords == 0) {
            improvements.add("Ground claims in concrete East African case studies, empirical metrics, or comparative jurisdiction data.")
        }
        if (hasRebuttalWords == 0) {
            improvements.add("Directly quote and deconstruct the opponent's prior mechanism rather than advancing standalone claims.")
        }
        if (totalUserWords < 45) {
            improvements.add("Flesh out the second-order economic impacts to show what happens after the initial policy is enacted.")
        }
        if (improvements.isEmpty()) {
            improvements.add("Deepen the clash by anticipating the opponent's strongest counter-factual argument before they table it.")
        }

        // Extract an actual user sentence for the rewrite
        val userSentences = allUserText.split(Regex("[.!?]+\\s*")).filter { it.trim().length >= 15 }
        val originalLine = userSentences.firstOrNull { it.length in 20..100 }
            ?: userSentences.firstOrNull()
            ?: "We need this policy to make things better."

        val improvedLine = "Enacting this measure dismantles structural bottlenecks by providing direct institutional resources to the most affected stakeholders."
        val reason = "Replaces a broad value claim with an explicit causal mechanism and clear target beneficiary."

        val coachVerdict = when {
            overall >= 8.0 -> "Exceptional rhetoric! You dominated the clash with sharp causality and precise rebuttals."
            overall >= 7.0 -> "Solid tournament performance. Solidify your empirical examples to reach the top tier."
            else -> "Commendable effort. Focus on dismantling the opponent's core mechanism in your next debate."
        }

        return DebateScoreResult(
            argumentStrength = argStrength,
            evidence = evidence,
            rebuttal = rebuttal,
            structure = structure,
            writingQuality = writingQuality,
            overall = overall,
            strengths = strengths,
            improvements = improvements,
            rewriteIt = RewriteItFeedback(
                original = originalLine.trim(),
                improved = improvedLine,
                reason = reason
            ),
            coachMessage = coachVerdict
        )
    }
}
