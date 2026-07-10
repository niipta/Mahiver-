package com.example.data

import android.content.Context
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import java.util.UUID

/**
 * JSON schema for syllabus import / export.
 *
 * Sample:
 * {
 *   "subjects": [
 *     {
 *       "name": "Physics",
 *       "color": "#3B82F6",
 *       "icon": "Science",
 *       "topics": [
 *         {
 *           "name": "Mechanics",
 *           "estimatedMinutes": 120,
 *           "isPriority": false,
 *           "isWeak": false,
 *           "subtopics": ["Newton's Laws", "Energy", "Momentum"]
 *         }
 *       ]
 *     }
 *   ]
 * }
 */
@JsonClass(generateAdapter = true)
data class SyllabusJson(
    val subjects: List<SubjectJson> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SubjectJson(
    val name: String,
    val color: String? = null,
    val icon: String? = null,
    val topics: List<TopicJson> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TopicJson(
    val name: String,
    val estimatedMinutes: Int = 30,
    val isPriority: Boolean = false,
    val isWeak: Boolean = false,
    val subtopics: List<String> = emptyList()
)

object SyllabusImporter {

    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val adapter by lazy { moshi.adapter(SyllabusJson::class.java) }

    /**
     * Parse a JSON string into a SyllabusJson model.
     * Throws [IllegalArgumentException] if JSON is malformed.
     */
    fun parse(json: String): SyllabusJson {
        val result = adapter.fromJson(json)
            ?: throw IllegalArgumentException("Empty or invalid JSON")
        if (result.subjects.isEmpty()) {
            throw IllegalArgumentException("JSON contains no subjects")
        }
        return result
    }

    /**
     * Import the parsed syllabus into the database in a single transaction.
     * Returns the number of subjects + topics + subtopics created.
     */
    suspend fun import(
        context: Context,
        parsed: SyllabusJson
    ): Triple<Int, Int, Int> = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.syllabusDao()
        val syncDao = db.syncDao()
        val batch = mutableListOf<SyncQueueEntity>()

        var subjectsCreated = 0
        var topicsCreated = 0
        var subtopicsCreated = 0

        db.withTransaction {
            for (subjectJson in parsed.subjects) {
                val existing = dao.getAllSubjectsSync().firstOrNull {
                    it.name.equals(subjectJson.name, ignoreCase = true)
                }
                if (existing != null) {
                    // Skip duplicates — preserve existing subject
                    continue
                }

                val subjectId = UUID.randomUUID().toString()
                val colorLong = parseColor(subjectJson.color, 0xFF3B82F6L)
                val subject = SubjectEntity(
                    id = subjectId,
                    name = subjectJson.name,
                    icon = subjectJson.icon ?: "Book",
                    color = colorLong
                )
                dao.insertSubject(subject)
                batch.add(
                    SyncQueueEntity(
                        operationType = "INSERT",
                        entityType = "SUBJECT",
                        entityId = subjectId
                    )
                )
                subjectsCreated++

                for (topicJson in subjectJson.topics) {
                    val topicId = UUID.randomUUID().toString()
                    val topic = TopicEntity(
                        id = topicId,
                        subjectId = subjectId,
                        name = topicJson.name,
                        isPriority = topicJson.isPriority,
                        isWeak = topicJson.isWeak,
                        estimatedMinutes = topicJson.estimatedMinutes.coerceAtLeast(1),
                        isCompleted = false
                    )
                    dao.insertTopic(topic)
                    batch.add(
                        SyncQueueEntity(
                            operationType = "INSERT",
                            entityType = "TOPIC",
                            entityId = topicId
                        )
                    )
                    topicsCreated++

                    for (subName in topicJson.subtopics) {
                        val subId = UUID.randomUUID().toString()
                        val subtopic = SubtopicEntity(
                            id = subId,
                            topicId = topicId,
                            name = subName,
                            isCompleted = false
                        )
                        dao.insertSubtopic(subtopic)
                        batch.add(
                            SyncQueueEntity(
                                operationType = "INSERT",
                                entityType = "SUBTOPIC",
                                entityId = subId
                            )
                        )
                        subtopicsCreated++
                    }
                }
            }
            syncDao.insertSyncTasks(batch)
        }

        Triple(subjectsCreated, topicsCreated, subtopicsCreated)
    }

    /**
     * Accepts #RRGGBB, #AARRGGBB, or named colors. Falls back to [defaultColor].
     */
    private fun parseColor(hex: String?, defaultColor: Long): Long {
        if (hex.isNullOrBlank()) return defaultColor
        return try {
            val normalized = hex.removePrefix("#")
            when (normalized.length) {
                6 -> ("FF$normalized").toLong(16)
                8 -> normalized.toLong(16)
                else -> defaultColor
            }
        } catch (_: Throwable) {
            defaultColor
        }
    }

    /**
     * Built-in syllabus templates that can be imported with one tap.
     */
    val templates: List<SyllabusTemplate> = listOf(
        SyllabusTemplate(
            id = "jee_mains",
            name = "JEE Mains",
            description = "Physics, Chemistry, Mathematics — full syllabus",
            emoji = "M",
            accentColor = 0xFF3B82F6L,
            json = jeeMainsJson
        ),
        SyllabusTemplate(
            id = "neet",
            name = "NEET",
            description = "Physics, Chemistry, Biology — full syllabus",
            emoji = "N",
            accentColor = 0xFF10B981L,
            json = neetJson
        ),
        SyllabusTemplate(
            id = "upsc_cse",
            name = "UPSC CSE",
            description = "GS Paper I-IV + CSAT prelims",
            emoji = "U",
            accentColor = 0xFFF59E0BL,
            json = upscJson
        ),
        SyllabusTemplate(
            id = "class10_cbse",
            name = "Class 10 CBSE",
            description = "Maths, Science, SST, English",
            emoji = "10",
            accentColor = 0xFF8B5CF6L,
            json = class10Json
        ),
        SyllabusTemplate(
            id = "gate_cse",
            name = "GATE CSE",
            description = "Computer Science Engineering topics",
            emoji = "G",
            accentColor = 0xFFEF4444L,
            json = gateCseJson
        ),
        SyllabusTemplate(
            id = "ssc_cgl",
            name = "SSC CGL",
            description = "Tier 1 + Tier 2 subjects",
            emoji = "S",
            accentColor = 0xFFEC4899L,
            json = sscCglJson
        )
    )
}

data class SyllabusTemplate(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val accentColor: Long,
    val json: String
)

// ============================================================
// Built-in JSON templates
// ============================================================

private val jeeMainsJson = """
{
  "subjects": [
    {
      "name": "Physics",
      "color": "#3B82F6",
      "icon": "Science",
      "topics": [
        { "name": "Units, Dimensions & Measurement", "estimatedMinutes": 90, "subtopics": ["SI Units", "Dimensional Analysis", "Errors"] },
        { "name": "Kinematics", "estimatedMinutes": 120, "subtopics": ["Motion in Straight Line", "Projectile Motion", "Relative Velocity"] },
        { "name": "Laws of Motion", "estimatedMinutes": 120, "subtopics": ["Newton's Laws", "Friction", "Pseudo Forces"] },
        { "name": "Work, Energy & Power", "estimatedMinutes": 100, "subtopics": ["Work-Energy Theorem", "Conservation", "Collisions"] },
        { "name": "Rotational Motion", "estimatedMinutes": 150, "subtopics": ["Moment of Inertia", "Torque", "Angular Momentum"] },
        { "name": "Gravitation", "estimatedMinutes": 90, "subtopics": ["Kepler's Laws", "Gravitational Field", "Escape Velocity"] },
        { "name": "Thermodynamics", "estimatedMinutes": 150, "subtopics": ["First Law", "Second Law", "Carnot Cycle"] },
        { "name": "Oscillations & Waves", "estimatedMinutes": 150, "subtopics": ["SHM", "Damped Oscillations", "Sound Waves"] },
        { "name": "Electrostatics", "estimatedMinutes": 180, "subtopics": ["Coulomb's Law", "Electric Field", "Gauss's Law"] },
        { "name": "Current Electricity", "estimatedMinutes": 120, "subtopics": ["Ohm's Law", "Kirchhoff's Laws", "Wheatstone Bridge"] },
        { "name": "Magnetism", "estimatedMinutes": 150, "subtopics": ["Biot-Savart Law", "Ampere's Law", "Magnetic Force"] },
        { "name": "EM Induction & AC", "estimatedMinutes": 120, "subtopics": ["Faraday's Law", "Lenz's Law", "AC Circuits"] },
        { "name": "Optics", "estimatedMinutes": 150, "subtopics": ["Ray Optics", "Wave Optics", "Interference"] },
        { "name": "Modern Physics", "estimatedMinutes": 180, "subtopics": ["Photoelectric Effect", "Bohr Model", "Nuclear Physics"] }
      ]
    },
    {
      "name": "Chemistry",
      "color": "#10B981",
      "icon": "Science",
      "topics": [
        { "name": "Some Basic Concepts of Chemistry", "estimatedMinutes": 90, "subtopics": ["Mole Concept", "Stoichiometry", "Empirical Formula"] },
        { "name": "Atomic Structure", "estimatedMinutes": 120, "subtopics": ["Bohr's Model", "Quantum Numbers", "Electronic Configuration"] },
        { "name": "Chemical Bonding", "estimatedMinutes": 150, "subtopics": ["VSEPR", "Hybridization", "Molecular Orbital Theory"] },
        { "name": "Thermodynamics", "estimatedMinutes": 150, "subtopics": ["Enthalpy", "Entropy", "Gibbs Energy"] },
        { "name": "Equilibrium", "estimatedMinutes": 120, "subtopics": ["Le Chatelier's Principle", "Ionic Equilibrium", "pH"] },
        { "name": "Redox Reactions", "estimatedMinutes": 80, "subtopics": ["Oxidation Number", "Balancing", "Electrochemistry"] },
        { "name": "Periodic Table", "estimatedMinutes": 100, "subtopics": ["Periodic Trends", "Periodicity", "Group Trends"] },
        { "name": "p-Block Elements", "estimatedMinutes": 180, "subtopics": ["Group 13", "Group 14", "Group 15-18"] },
        { "name": "d & f-Block Elements", "estimatedMinutes": 120, "subtopics": ["Transition Metals", "Lanthanides", "Actinides"] },
        { "name": "Coordination Compounds", "estimatedMinutes": 150, "subtopics": ["Werner's Theory", "Isomerism", "Crystal Field Theory"] },
        { "name": "Haloalkanes & Haloarenes", "estimatedMinutes": 100, "subtopics": ["SN1", "SN2", "Reactions"] },
        { "name": "Alcohols, Phenols & Ethers", "estimatedMinutes": 100, "subtopics": ["Properties", "Reactions", "Uses"] },
        { "name": "Aldehydes, Ketones & Acids", "estimatedMinutes": 120, "subtopics": ["Preparation", "Reactions", "Mechanisms"] },
        { "name": "Biomolecules", "estimatedMinutes": 90, "subtopics": ["Carbohydrates", "Proteins", "Nucleic Acids"] }
      ]
    },
    {
      "name": "Mathematics",
      "color": "#F59E0B",
      "icon": "Calculate",
      "topics": [
        { "name": "Sets, Relations & Functions", "estimatedMinutes": 100, "subtopics": ["Sets", "Relations", "Functions"] },
        { "name": "Complex Numbers & Quadratic Equations", "estimatedMinutes": 150, "subtopics": ["Algebra", "Argand Plane", "Quadratic"] },
        { "name": "Matrices & Determinants", "estimatedMinutes": 120, "subtopics": ["Matrix Algebra", "Determinants", "Inverse"] },
        { "name": "Permutations & Combinations", "estimatedMinutes": 90, "subtopics": ["Counting", "Permutations", "Combinations"] },
        { "name": "Binomial Theorem", "estimatedMinutes": 80, "subtopics": ["Expansion", "General Term", "Properties"] },
        { "name": "Sequences & Series", "estimatedMinutes": 100, "subtopics": ["AP", "GP", "HP"] },
        { "name": "Trigonometry", "estimatedMinutes": 150, "subtopics": ["Identities", "Equations", "Inverse Trig"] },
        { "name": "Straight Lines", "estimatedMinutes": 100, "subtopics": ["Slope", "Equations", "Distance"] },
        { "name": "Conic Sections", "estimatedMinutes": 180, "subtopics": ["Circle", "Parabola", "Ellipse", "Hyperbola"] },
        { "name": "Limits & Continuity", "estimatedMinutes": 120, "subtopics": ["Limits", "Continuity", "Differentiability"] },
        { "name": "Differentiation", "estimatedMinutes": 150, "subtopics": ["Rules", "Implicit", "Higher Order"] },
        { "name": "Application of Derivatives", "estimatedMinutes": 150, "subtopics": ["Rate", "Maxima-Minima", "Tangents"] },
        { "name": "Integration", "estimatedMinutes": 180, "subtopics": ["Indefinite", "Definite", "Methods"] },
        { "name": "Application of Integrals", "estimatedMinutes": 120, "subtopics": ["Area", "Volume", "Surface"] },
        { "name": "Differential Equations", "estimatedMinutes": 120, "subtopics": ["Order", "Solution", "Linear"] },
        { "name": "Probability", "estimatedMinutes": 120, "subtopics": ["Bayes Theorem", "Distributions", "Random Variables"] },
        { "name": "3D Geometry & Vectors", "estimatedMinutes": 150, "subtopics": ["Vectors", "Lines", "Planes"] }
      ]
    }
  ]
}
""".trimIndent()

private val neetJson = """
{
  "subjects": [
    {
      "name": "Physics",
      "color": "#3B82F6",
      "icon": "Science",
      "topics": [
        { "name": "Mechanics", "estimatedMinutes": 180, "subtopics": ["Kinematics", "Laws of Motion", "Work-Energy"] },
        { "name": "Thermodynamics", "estimatedMinutes": 120, "subtopics": ["Laws", "Carnot Cycle", "Entropy"] },
        { "name": "Optics", "estimatedMinutes": 150, "subtopics": ["Ray Optics", "Wave Optics"] },
        { "name": "Electrostatics & Current", "estimatedMinutes": 180, "subtopics": ["Field", "Potential", "Circuits"] },
        { "name": "Modern Physics", "estimatedMinutes": 150, "subtopics": ["Atoms", "Nuclei", "Semiconductors"] }
      ]
    },
    {
      "name": "Chemistry",
      "color": "#10B981",
      "icon": "Science",
      "topics": [
        { "name": "Physical Chemistry", "estimatedMinutes": 200, "subtopics": ["Mole", "Thermo", "Equilibrium", "Kinetics"] },
        { "name": "Inorganic Chemistry", "estimatedMinutes": 200, "subtopics": ["Periodic", "p-Block", "d-Block", "Coordination"] },
        { "name": "Organic Chemistry", "estimatedMinutes": 240, "subtopics": ["GOC", "Hydrocarbons", "Functional Groups", "Biomolecules"] }
      ]
    },
    {
      "name": "Biology",
      "color": "#22C55E",
      "icon": "Biotech",
      "topics": [
        { "name": "Cell Biology", "estimatedMinutes": 120, "subtopics": ["Cell Structure", "Cell Cycle", "Biomolecules"] },
        { "name": "Genetics & Evolution", "estimatedMinutes": 180, "subtopics": ["Mendelian", "Molecular Basis", "Evolution"] },
        { "name": "Plant Physiology", "estimatedMinutes": 120, "subtopics": ["Photosynthesis", "Respiration", "Transport"] },
        { "name": "Human Physiology", "estimatedMinutes": 200, "subtopics": ["Digestion", "Respiration", "Circulation", "Excretion", "Nervous"] },
        { "name": "Reproduction", "estimatedMinutes": 120, "subtopics": ["Human", "Plants", "Reproductive Health"] },
        { "name": "Ecology & Environment", "estimatedMinutes": 100, "subtopics": ["Ecosystem", "Biodiversity", "Environmental Issues"] },
        { "name": "Biotechnology", "estimatedMinutes": 100, "subtopics": ["Principles", "Applications"] }
      ]
    }
  ]
}
""".trimIndent()

private val upscJson = """
{
  "subjects": [
    {
      "name": "Indian Polity",
      "color": "#3B82F6",
      "icon": "AccountBalance",
      "topics": [
        { "name": "Constitution", "estimatedMinutes": 180, "subtopics": ["Preamble", "Fundamental Rights", "DPSP"] },
        { "name": "Union Executive", "estimatedMinutes": 120, "subtopics": ["President", "PM", "Council of Ministers"] },
        { "name": "Parliament", "estimatedMinutes": 150, "subtopics": ["Lok Sabha", "Rajya Sabha", "Legislative Process"] },
        { "name": "Judiciary", "estimatedMinutes": 120, "subtopics": ["Supreme Court", "High Court", "Judicial Review"] },
        { "name": "Federalism", "estimatedMinutes": 100, "subtopics": ["Centre-State Relations", "Inter-State Council", "Finance Commission"] }
      ]
    },
    {
      "name": "Modern History",
      "color": "#F59E0B",
      "icon": "History",
      "topics": [
        { "name": "Advent of British", "estimatedMinutes": 120, "subtopics": ["East India Company", "Battles", "Policies"] },
        { "name": "Revolt of 1857", "estimatedMinutes": 90, "subtopics": ["Causes", "Events", "Impact"] },
        { "name": "Indian National Movement", "estimatedMinutes": 240, "subtopics": ["INC Formation", "Moderates", "Extremists", "Gandhian Era"] },
        { "name": "Post-Independence", "estimatedMinutes": 100, "subtopics": ["Integration", "Constitution Making", "Nehru Era"] }
      ]
    },
    {
      "name": "Geography",
      "color": "#10B981",
      "icon": "Public",
      "topics": [
        { "name": "Physical Geography", "estimatedMinutes": 180, "subtopics": ["Geomorphology", "Climatology", "Oceanography"] },
        { "name": "Indian Geography", "estimatedMinutes": 150, "subtopics": ["Physiography", "Drainage", "Climate"] },
        { "name": "Economic Geography", "estimatedMinutes": 120, "subtopics": ["Agriculture", "Industries", "Transport"] },
        { "name": "World Geography", "estimatedMinutes": 120, "subtopics": ["Continents", "Rivers", "Mountains"] }
      ]
    },
    {
      "name": "Economy",
      "color": "#EF4444",
      "icon": "TrendingUp",
      "topics": [
        { "name": "Basic Concepts", "estimatedMinutes": 120, "subtopics": ["GDP", "Inflation", "Fiscal Policy"] },
        { "name": "Banking & Finance", "estimatedMinutes": 150, "subtopics": ["RBI", "Monetary Policy", "Banking System"] },
        { "name": "Budget", "estimatedMinutes": 100, "subtopics": ["Union Budget", "Railway Budget", "Taxes"] },
        { "name": "External Sector", "estimatedMinutes": 90, "subtopics": ["BoP", "FDI", "EXIM Policy"] }
      ]
    },
    {
      "name": "Environment & Ecology",
      "color": "#22C55E",
      "icon": "Eco",
      "topics": [
        { "name": "Ecology Basics", "estimatedMinutes": 100, "subtopics": ["Ecosystem", "Food Chain", "Biogeochemical Cycles"] },
        { "name": "Biodiversity", "estimatedMinutes": 90, "subtopics": ["Types", "Hotspots", "Conservation"] },
        { "name": "Environmental Issues", "estimatedMinutes": 120, "subtopics": ["Pollution", "Climate Change", "Conservation Acts"] }
      ]
    },
    {
      "name": "CSAT",
      "color": "#8B5CF6",
      "icon": "Quiz",
      "topics": [
        { "name": "Comprehension", "estimatedMinutes": 120, "subtopics": ["Reading", "Inference"] },
        { "name": "Quantitative Aptitude", "estimatedMinutes": 180, "subtopics": ["Numbers", "Time-Speed", "Geometry"] },
        { "name": "Logical Reasoning", "estimatedMinutes": 150, "subtopics": ["Syllogism", "Series", "Puzzles"] },
        { "name": "Data Interpretation", "estimatedMinutes": 90, "subtopics": ["Tables", "Charts", "Graphs"] }
      ]
    }
  ]
}
""".trimIndent()

private val class10Json = """
{
  "subjects": [
    {
      "name": "Mathematics",
      "color": "#3B82F6",
      "icon": "Calculate",
      "topics": [
        { "name": "Real Numbers", "estimatedMinutes": 90, "subtopics": ["Euclid's Division", "Fundamental Theorem"] },
        { "name": "Polynomials", "estimatedMinutes": 90, "subtopics": ["Zeros", "Division", "Factor Theorem"] },
        { "name": "Pair of Linear Equations", "estimatedMinutes": 120, "subtopics": ["Graphical", "Substitution", "Elimination"] },
        { "name": "Quadratic Equations", "estimatedMinutes": 120, "subtopics": ["Solution", "Discriminant", "Word Problems"] },
        { "name": "Arithmetic Progressions", "estimatedMinutes": 90, "subtopics": ["nth Term", "Sum"] },
        { "name": "Triangles", "estimatedMinutes": 150, "subtopics": ["Similarity", "Pythagoras", "Areas"] },
        { "name": "Coordinate Geometry", "estimatedMinutes": 120, "subtopics": ["Distance", "Section", "Area"] },
        { "name": "Trigonometry", "estimatedMinutes": 120, "subtopics": ["Ratios", "Identities", "Applications"] },
        { "name": "Circles", "estimatedMinutes": 90, "subtopics": ["Tangents", "Properties"] },
        { "name": "Surface Areas & Volumes", "estimatedMinutes": 120, "subtopics": ["Cube", "Cylinder", "Cone", "Sphere"] },
        { "name": "Statistics", "estimatedMinutes": 90, "subtopics": ["Mean", "Median", "Mode"] },
        { "name": "Probability", "estimatedMinutes": 60, "subtopics": ["Classical", "Events"] }
      ]
    },
    {
      "name": "Science",
      "color": "#10B981",
      "icon": "Science",
      "topics": [
        { "name": "Chemical Reactions", "estimatedMinutes": 90, "subtopics": ["Types", "Oxidation-Reduction", "Effects"] },
        { "name": "Acids, Bases & Salts", "estimatedMinutes": 100, "subtopics": ["Properties", "pH", "Salt Formation"] },
        { "name": "Metals & Non-Metals", "estimatedMinutes": 120, "subtopics": ["Properties", "Reactivity", "Extraction"] },
        { "name": "Carbon Compounds", "estimatedMinutes": 120, "subtopics": ["Bonding", "Hydrocarbons", "Ethanol", "Soaps"] },
        { "name": "Life Processes", "estimatedMinutes": 150, "subtopics": ["Nutrition", "Respiration", "Transportation", "Excretion"] },
        { "name": "Control & Coordination", "estimatedMinutes": 100, "subtopics": ["Nervous", "Hormones", "Plant Coordination"] },
        { "name": "Reproduction", "estimatedMinutes": 100, "subtopics": ["Asexual", "Sexual", "Reproductive Health"] },
        { "name": "Heredity & Evolution", "estimatedMinutes": 90, "subtopics": ["Mendel", "Sex Determination", "Evolution"] },
        { "name": "Light: Reflection & Refraction", "estimatedMinutes": 120, "subtopics": ["Mirrors", "Lenses", "Power"] },
        { "name": "Electricity", "estimatedMinutes": 120, "subtopics": ["Ohm's Law", "Circuits", "Heating Effect"] },
        { "name": "Magnetic Effects of Current", "estimatedMinutes": 90, "subtopics": ["Field", "Solenoid", "Domestic Circuits"] },
        { "name": "Environment", "estimatedMinutes": 80, "subtopics": ["Ecosystem", "Conservation", "Sustainability"] }
      ]
    },
    {
      "name": "Social Science",
      "color": "#F59E0B",
      "icon": "History",
      "topics": [
        { "name": "Nationalism in India", "estimatedMinutes": 120, "subtopics": ["Non-Cooperation", "Civil Disobedience", "Quit India"] },
        { "name": "Resources & Development", "estimatedMinutes": 90, "subtopics": ["Types", "Planning", "Conservation"] },
        { "name": "Power Sharing", "estimatedMinutes": 80, "subtopics": ["Belgium", "Sri Lanka", "Forms"] },
        { "name": "Outcomes of Democracy", "estimatedMinutes": 80, "subtopics": ["Accountability", "Equality"] },
        { "name": "Development", "estimatedMinutes": 90, "subtopics": ["Indicators", "HDI", "Sustainability"] },
        { "name": "Sectors of Indian Economy", "estimatedMinutes": 80, "subtopics": ["Primary", "Secondary", "Tertiary"] },
        { "name": "Money & Credit", "estimatedMinutes": 80, "subtopics": ["Money Functions", "Banking", "Loans"] }
      ]
    },
    {
      "name": "English",
      "color": "#8B5CF6",
      "icon": "MenuBook",
      "topics": [
        { "name": "Reading Comprehension", "estimatedMinutes": 120, "subtopics": ["Unseen Passages", "Case-Based"] },
        { "name": "Writing & Grammar", "estimatedMinutes": 120, "subtopics": ["Letters", "Analysis", "Tenses"] },
        { "name": "First Flight (Prose)", "estimatedMinutes": 150, "subtopics": ["Letter to God", "Nelson Mandela", "Glimpses of India"] },
        { "name": "First Flight (Poetry)", "estimatedMinutes": 90, "subtopics": ["Dust of Snow", "Fire and Ice", "The Trees"] },
        { "name": "Footprints Without Feet", "estimatedMinutes": 120, "subtopics": ["Thief's Story", "Midnight Visitor", "Necklace"] }
      ]
    }
  ]
}
""".trimIndent()

private val gateCseJson = """
{
  "subjects": [
    {
      "name": "Engineering Mathematics",
      "color": "#3B82F6",
      "icon": "Calculate",
      "topics": [
        { "name": "Discrete Mathematics", "estimatedMinutes": 240, "subtopics": ["Logic", "Set Theory", "Graph Theory", "Combinatorics"] },
        { "name": "Linear Algebra", "estimatedMinutes": 120, "subtopics": ["Matrices", "Eigenvalues", "Systems of Equations"] },
        { "name": "Calculus", "estimatedMinutes": 120, "subtopics": ["Limits", "Differentiation", "Integration"] },
        { "name": "Probability & Statistics", "estimatedMinutes": 150, "subtopics": ["Distributions", "Mean/Median", "Bayes"] }
      ]
    },
    {
      "name": "Digital Logic",
      "color": "#10B981",
      "icon": "Memory",
      "topics": [
        { "name": "Boolean Algebra", "estimatedMinutes": 120, "subtopics": ["Laws", "K-Map", "Minimization"] },
        { "name": "Combinational Circuits", "estimatedMinutes": 150, "subtopics": ["Adders", "Multiplexers", "Encoders"] },
        { "name": "Sequential Circuits", "estimatedMinutes": 150, "subtopics": ["Flip-Flops", "Counters", "Registers"] }
      ]
    },
    {
      "name": "Computer Organization & Architecture",
      "color": "#F59E0B",
      "icon": "Memory",
      "topics": [
        { "name": "Machine Instructions", "estimatedMinutes": 100, "subtopics": ["Addressing Modes", "Instruction Cycle"] },
        { "name": "ALU & Datapath", "estimatedMinutes": 120, "subtopics": ["ALU Design", "Control Unit"] },
        { "name": "Memory Hierarchy", "estimatedMinutes": 150, "subtopics": ["Cache", "Main Memory", "Virtual Memory"] },
        { "name": "Pipelining", "estimatedMinutes": 100, "subtopics": ["Hazards", "Branch Prediction"] }
      ]
    },
    {
      "name": "Programming & Data Structures",
      "color": "#EF4444",
      "icon": "Code",
      "topics": [
        { "name": "C Programming", "estimatedMinutes": 180, "subtopics": ["Pointers", "Functions", "Structures"] },
        { "name": "Arrays & Strings", "estimatedMinutes": 100, "subtopics": ["Operations", "Searching", "Sorting"] },
        { "name": "Linked Lists", "estimatedMinutes": 120, "subtopics": ["Singly", "Doubly", "Circular"] },
        { "name": "Stacks & Queues", "estimatedMinutes": 120, "subtopics": ["Operations", "Applications"] },
        { "name": "Trees", "estimatedMinutes": 180, "subtopics": ["BST", "AVL", "Traversals", "Heaps"] },
        { "name": "Graphs", "estimatedMinutes": 200, "subtopics": ["Traversals", "Shortest Path", "MST"] },
        { "name": "Hashing", "estimatedMinutes": 90, "subtopics": ["Hash Functions", "Collision Resolution"] }
      ]
    },
    {
      "name": "Algorithms",
      "color": "#8B5CF6",
      "icon": "Functions",
      "topics": [
        { "name": "Asymptotic Analysis", "estimatedMinutes": 90, "subtopics": ["Big-O", "Master Theorem"] },
        { "name": "Searching & Sorting", "estimatedMinutes": 150, "subtopics": ["Binary Search", "Quick Sort", "Merge Sort"] },
        { "name": "Divide & Conquer", "estimatedMinutes": 90, "subtopics": ["Recurrences", "Applications"] },
        { "name": "Greedy Algorithms", "estimatedMinutes": 120, "subtopics": ["Huffman", "Activity Selection", "Kruskal"] },
        { "name": "Dynamic Programming", "estimatedMinutes": 180, "subtopics": ["LCS", "Matrix Chain", "0/1 Knapsack"] },
        { "name": "Graph Algorithms", "estimatedMinutes": 180, "subtopics": ["BFS", "DFS", "Dijkstra", "Bellman-Ford"] }
      ]
    },
    {
      "name": "Theory of Computation",
      "color": "#EC4899",
      "icon": "Functions",
      "topics": [
        { "name": "Finite Automata", "estimatedMinutes": 150, "subtopics": ["DFA", "NFA", "Minimization"] },
        { "name": "Regular Expressions", "estimatedMinutes": 100, "subtopics": ["Properties", "Conversion"] },
        { "name": "Context Free Grammars", "estimatedMinutes": 150, "subtopics": ["Parsers", "Ambiguity", "CNF"] },
        { "name": "Turing Machines", "estimatedMinutes": 120, "subtopics": ["Design", "Halting Problem"] }
      ]
    },
    {
      "name": "Compiler Design",
      "color": "#14B8A6",
      "icon": "Code",
      "topics": [
        { "name": "Lexical Analysis", "estimatedMinutes": 100, "subtopics": ["Tokens", "RE to DFA"] },
        { "name": "Parsing", "estimatedMinutes": 180, "subtopics": ["Top-Down", "Bottom-Up", "LR"] },
        { "name": "Syntax-Directed Translation", "estimatedMinutes": 100, "subtopics": ["SDD", "SDT"] },
        { "name": "Code Generation", "estimatedMinutes": 100, "subtopics": ["Intermediate", "Optimization"] }
      ]
    },
    {
      "name": "Operating System",
      "color": "#6366F1",
      "icon": "Settings",
      "topics": [
        { "name": "Process Management", "estimatedMinutes": 150, "subtopics": ["Scheduling", "Threads", "PCB"] },
        { "name": "Synchronization", "estimatedMinutes": 180, "subtopics": ["Semaphores", "Monitors", "Deadlocks"] },
        { "name": "Memory Management", "estimatedMinutes": 150, "subtopics": ["Paging", "Segmentation", "Virtual Memory"] },
        { "name": "File System", "estimatedMinutes": 100, "subtopics": ["Allocation", "Directory Structure"] }
      ]
    },
    {
      "name": "Databases",
      "color": "#F97316",
      "icon": "Storage",
      "topics": [
        { "name": "ER Model", "estimatedMinutes": 100, "subtopics": ["Diagram", "Keys", "Normalization"] },
        { "name": "Relational Model", "estimatedMinutes": 120, "subtopics": ["Algebra", "Calculus"] },
        { "name": "SQL", "estimatedMinutes": 150, "subtopics": ["DDL", "DML", "Joins", "Aggregation"] },
        { "name": "Transactions", "estimatedMinutes": 120, "subtopics": ["ACID", "Concurrency", "Recovery"] },
        { "name": "Indexing", "estimatedMinutes": 90, "subtopics": ["B+ Trees", "Hash Index"] }
      ]
    },
    {
      "name": "Computer Networks",
      "color": "#84CC16",
      "icon": "Wifi",
      "topics": [
        { "name": "Physical Layer", "estimatedMinutes": 120, "subtopics": ["Transmission", "Encoding", "Multiplexing"] },
        { "name": "Data Link Layer", "estimatedMinutes": 150, "subtopics": ["Framing", "Error Detection", "MAC"] },
        { "name": "Network Layer", "estimatedMinutes": 180, "subtopics": ["IP", "Routing", "Subnetting"] },
        { "name": "Transport Layer", "estimatedMinutes": 150, "subtopics": ["TCP", "UDP", "Congestion Control"] },
        { "name": "Application Layer", "estimatedMinutes": 100, "subtopics": ["HTTP", "DNS", "Email"] }
      ]
    }
  ]
}
""".trimIndent()

private val sscCglJson = """
{
  "subjects": [
    {
      "name": "Quantitative Aptitude",
      "color": "#3B82F6",
      "icon": "Calculate",
      "topics": [
        { "name": "Number System", "estimatedMinutes": 90, "subtopics": ["Divisibility", "LCM/HCF", "Simplification"] },
        { "name": "Percentages", "estimatedMinutes": 80, "subtopics": ["Basic", "Successive", "Elections"] },
        { "name": "Profit & Loss", "estimatedMinutes": 100, "subtopics": ["Discount", "Markup", "Dishonest Dealer"] },
        { "name": "Simple & Compound Interest", "estimatedMinutes": 90, "subtopics": ["SI", "CI", "Installments"] },
        { "name": "Ratio & Proportion", "estimatedMinutes": 80, "subtopics": ["Mixtures", "Partnership"] },
        { "name": "Time & Work", "estimatedMinutes": 100, "subtopics": ["Men-Days", "Pipes", "Alternate Work"] },
        { "name": "Time, Speed & Distance", "estimatedMinutes": 100, "subtopics": ["Trains", "Boats", "Races"] },
        { "name": "Geometry", "estimatedMinutes": 120, "subtopics": ["Triangles", "Circles", "Polygons"] },
        { "name": "Mensuration", "estimatedMinutes": 100, "subtopics": ["2D", "3D"] },
        { "name": "Data Interpretation", "estimatedMinutes": 120, "subtopics": ["Tables", "Bar", "Pie"] }
      ]
    },
    {
      "name": "General Intelligence & Reasoning",
      "color": "#10B981",
      "icon": "Psychology",
      "topics": [
        { "name": "Verbal Reasoning", "estimatedMinutes": 150, "subtopics": ["Analogy", "Classification", "Series"] },
        { "name": "Non-Verbal Reasoning", "estimatedMinutes": 120, "subtopics": ["Paper Folding", "Mirror Image", "Embedded"] },
        { "name": "Logical Reasoning", "estimatedMinutes": 150, "subtopics": ["Syllogism", "Statements", "Blood Relations"] }
      ]
    },
    {
      "name": "English Language",
      "color": "#F59E0B",
      "icon": "Translate",
      "topics": [
        { "name": "Grammar", "estimatedMinutes": 120, "subtopics": ["Tenses", "Articles", "Prepositions"] },
        { "name": "Vocabulary", "estimatedMinutes": 120, "subtopics": ["Synonyms", "Antonyms", "One Word"] },
        { "name": "Comprehension", "estimatedMinutes": 100, "subtopics": ["Passages", "Cloze Test"] },
        { "name": "Sentence Improvement", "estimatedMinutes": 80, "subtopics": ["Error Spotting", "Rearrangement"] }
      ]
    },
    {
      "name": "General Awareness",
      "color": "#EF4444",
      "icon": "Public",
      "topics": [
        { "name": "History", "estimatedMinutes": 120, "subtopics": ["Ancient", "Medieval", "Modern"] },
        { "name": "Geography", "estimatedMinutes": 100, "subtopics": ["Indian", "World", "Physical"] },
        { "name": "Polity", "estimatedMinutes": 100, "subtopics": ["Constitution", "Parliament", "Judiciary"] },
        { "name": "Economy", "estimatedMinutes": 100, "subtopics": ["Banking", "Budget", "Schemes"] },
        { "name": "Science", "estimatedMinutes": 120, "subtopics": ["Physics", "Chemistry", "Biology"] },
        { "name": "Current Affairs", "estimatedMinutes": 150, "subtopics": ["National", "International", "Sports"] }
      ]
    }
  ]
}
""".trimIndent()
