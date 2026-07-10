package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import androidx.room.withTransaction
import kotlinx.coroutines.withContext
import java.util.UUID

object ComputerSyllabusSeeder {
    suspend fun seed(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val dao = db.syllabusDao()
            val syncDao = db.syncDao()

            val existingSubjects = dao.getAllSubjectsSync()
            if (existingSubjects.any { it.name == "COMPUTER PDF" || it.name == "Computer Science" }) {
                return@withContext
            }

            val batchTasks = mutableListOf<SyncQueueEntity>()
            db.withTransaction {
                val subjectId = UUID.randomUUID().toString()
                val subject = SubjectEntity(
                    id = subjectId,
                    name = "COMPUTER PDF",
                    icon = "Memory",
                    color = 0xFF2196F3
                )
                dao.insertSubject(subject)
                batchTasks.add(SyncQueueEntity(operationType = "INSERT", entityType = "SUBJECT", entityId = subjectId))

            val schedule = listOf(
                Pair("Week 1: Introduction to Computer Science", listOf(
                    "Day 1: Computer Introduction & Components (Pages: 5-15)",
                    "Day 2: CPU, ALU & Control Unit (Pages: 16-30)",
                    "Day 3: Motherboard & Characteristics (Pages: 31-45)",
                    "Day 4: History & 1st-2nd Generation (Pages: 46-57)",
                    "Day 5: 3rd, 4th & 5th Generation (Pages: 58-65)",
                    "Day 6: Classification of Computers (Pages: 66-78)"
                )),
                Pair("Week 2: Input/Output Devices & Memory", listOf(
                    "Day 7: Input Devices (Pages: 79-95)",
                    "Day 8: Output Devices (Pages: 96-111)",
                    "Day 9: Primary Memory (RAM/ROM) (Pages: 112-120)",
                    "Day 10: Secondary Memory (Pages: 121-130)",
                    "Day 11: Memory Measurement (Pages: 131-141)"
                )),
                Pair("Week 3: Software & Operating System", listOf(
                    "Day 12: Introduction to Software & Types (Pages: 142-160)",
                    "Day 13: Operating System Basics (Pages: 161-175)",
                    "Day 14: Windows, Unix & Linux Concepts (Pages: 176-190)",
                    "Day 15: Virtual Memory & Processors (Pages: 191-210)",
                    "Day 16: Application Software (Pages: 211-229)"
                )),
                Pair("Week 4: Programming Languages & DBMS", listOf(
                    "Day 17: Low Level vs High Level Languages (Pages: 230-245)",
                    "Day 18: Algorithms & Flowcharts (Pages: 246-260)",
                    "Day 19: Compiler, Interpreter & Assembler (Pages: 261-280)",
                    "Day 20: DBMS Concepts (Pages: 281-300)",
                    "Day 21: Relational Data Model (Pages: 301-326)"
                )),
                Pair("Week 5: Data Representation & Boolean", listOf(
                    "Day 22: Binary & Decimal Number System (Pages: 327-336)",
                    "Day 23: Octal, Hexadecimal & Conversions (Pages: 337-348)",
                    "Day 24: Logical Gates & Boolean Operators (Pages: 349-356)",
                    "Day 25: Truth Tables, Laws of Boolean (Pages: 357-361)"
                )),
                Pair("Week 6: Data Communication & Network", listOf(
                    "Day 26: Transmission Channel & Media (Pages: 362-380)",
                    "Day 27: Network Topology (Pages: 381-400)",
                    "Day 28: LAN, MAN, WAN & Network Security (Pages: 401-417)",
                    "Day 29: OSI Model & Risk Assessment (Pages: 418-430)",
                    "Day 30: Modems & Data Transmission (Pages: 431-440)"
                )),
                Pair("Week 7: Internet & Web Tech", listOf(
                    "Day 31: Search Engines, E-mail & Browsers (Pages: 441-470)",
                    "Day 32: E-commerce, E-banking & IoT (Pages: 471-500)",
                    "Day 33: HTML, JavaScript & Web Publishing (Pages: 501-530)",
                    "Day 34: MS Windows UI & Desktop Settings (Pages: 531-560)",
                    "Day 35: Control Panel & Shortcut Keys (Pages: 561-582)"
                )),
                Pair("Week 8: Productivity Tools & Advanced", listOf(
                    "Day 36: MS Word Processing Tools (Pages: 583-624)",
                    "Day 37: MS Excel Spreadsheets (Pages: 625-670)",
                    "Day 38: MS Power Point Presentations (Pages: 671-724)",
                    "Day 39: Data Structures (Pages: 725-780)",
                    "Day 40: Cryptography Basics (Pages: 781-840)",
                    "Day 41: Digital Financial Tools (Pages: 841-890)",
                    "Day 42: Abbreviations & Misc (Pages: 891-960)"
                ))
            )

            for (week in schedule) {
                val topicId = UUID.randomUUID().toString()
                val topic = TopicEntity(
                    id = topicId,
                    subjectId = subjectId,
                    name = week.first,
                    isPriority = false,
                    isWeak = false,
                    estimatedMinutes = week.second.size * 60,
                    isCompleted = false
                )
                dao.insertTopic(topic)
                batchTasks.add(SyncQueueEntity(operationType = "INSERT", entityType = "TOPIC", entityId = topicId))

                for (subName in week.second) {
                    val subId = UUID.randomUUID().toString()
                    val subtopic = SubtopicEntity(
                        id = subId,
                        topicId = topicId,
                        name = subName,
                        isCompleted = false
                    )
                    dao.insertSubtopic(subtopic)
                    batchTasks.add(SyncQueueEntity(operationType = "INSERT", entityType = "SUBTOPIC", entityId = subId))
                }
            }
            syncDao.insertSyncTasks(batchTasks)
        }
    }
}
}
