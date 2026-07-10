-- ==========================================
-- SUPABASE SCHEMA FOR MAHIRVERSE
-- ==========================================
-- Tech Stack: PostgreSQL
-- Purpose: Offline-First Android App Backend
-- ==========================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. USERS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    timezone TEXT DEFAULT 'UTC',
    onboarding_completed BOOLEAN DEFAULT false,
    last_sync_timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==========================================
-- 2. SUBJECTS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    subject_name TEXT NOT NULL,
    completion_percentage REAL DEFAULT 0.0,
    estimated_total_time INTEGER DEFAULT 0,
    color_tag TEXT,
    icon TEXT,
    priority_level INTEGER DEFAULT 1,
    is_archived BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

-- ==========================================
-- 3. TOPICS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subject_id UUID NOT NULL REFERENCES public.subjects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    topic_name TEXT NOT NULL,
    completion_status TEXT DEFAULT 'PENDING',
    completion_percentage REAL DEFAULT 0.0,
    estimated_time INTEGER DEFAULT 0,
    actual_study_time INTEGER DEFAULT 0,
    priority_level INTEGER DEFAULT 1,
    difficulty_level TEXT,
    weak_topic_score REAL DEFAULT 0.0,
    revision_interval INTEGER DEFAULT 0,
    last_studied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

-- ==========================================
-- 4. SUBTOPICS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.subtopics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_id UUID NOT NULL REFERENCES public.topics(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    subtopic_name TEXT NOT NULL,
    completion_status TEXT DEFAULT 'PENDING',
    estimated_time INTEGER DEFAULT 0,
    actual_study_time INTEGER DEFAULT 0,
    revision_due_date TIMESTAMP WITH TIME ZONE,
    last_revision_date TIMESTAMP WITH TIME ZONE,
    weak_topic_score REAL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

-- ==========================================
-- 5. REVISIONS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.revisions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    topic_id UUID REFERENCES public.topics(id) ON DELETE CASCADE,
    subtopic_id UUID REFERENCES public.subtopics(id) ON DELETE CASCADE,
    revision_type TEXT DEFAULT 'STANDARD',
    revision_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_status TEXT DEFAULT 'PENDING',
    confidence_score INTEGER,
    missed_revision_count INTEGER DEFAULT 0,
    spaced_repetition_interval INTEGER DEFAULT 1,
    next_revision_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

-- ==========================================
-- 6. FOCUS_SESSIONS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.focus_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    topic_id UUID REFERENCES public.topics(id) ON DELETE SET NULL,
    subtopic_id UUID REFERENCES public.subtopics(id) ON DELETE SET NULL,
    session_type TEXT DEFAULT 'POMODORO',
    duration INTEGER NOT NULL,
    completed_duration INTEGER DEFAULT 0,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    productivity_score INTEGER,
    interruption_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==========================================
-- 7. ANALYTICS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    total_focus_time INTEGER DEFAULT 0,
    total_revision_time INTEGER DEFAULT 0,
    total_study_time INTEGER DEFAULT 0,
    strongest_subject UUID REFERENCES public.subjects(id) ON DELETE SET NULL,
    weakest_topic UUID REFERENCES public.topics(id) ON DELETE SET NULL,
    consistency_score REAL DEFAULT 100.0,
    focus_streak INTEGER DEFAULT 0,
    revision_streak INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==========================================
-- 8. PLANNER_DATA TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.planner_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    generated_plan JSONB NOT NULL,
    ai_recommendations JSONB,
    workload_score REAL,
    weak_topic_priority JSONB,
    revision_priority JSONB,
    focus_recommendation TEXT,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==========================================
-- 9. APP_PREFERENCES TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.app_preferences (
    user_id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
    dark_mode BOOLEAN DEFAULT true,
    notification_preferences JSONB DEFAULT '{"push": true, "email": false}'::jsonb,
    pomodoro_duration INTEGER DEFAULT 25,
    break_duration INTEGER DEFAULT 5,
    deep_focus_mode BOOLEAN DEFAULT false,
    ai_enabled BOOLEAN DEFAULT true,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==========================================
-- 10. EXAMS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.exams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    exam_name TEXT NOT NULL,
    exam_date TIMESTAMP WITH TIME ZONE NOT NULL,
    priority_level INTEGER DEFAULT 1,
    target_score REAL,
    countdown_visibility BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE DEFAULT NULL
);

-- ==========================================
-- 11. WEAK_TOPICS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.weak_topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES public.topics(id) ON DELETE CASCADE,
    weak_score REAL DEFAULT 0.0,
    missed_revisions INTEGER DEFAULT 0,
    incomplete_sessions INTEGER DEFAULT 0,
    recommendation_priority INTEGER DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==========================================
-- 12. SYNC_QUEUE TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS public.sync_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    operation_type TEXT NOT NULL, -- INSERT, UPDATE, DELETE
    entity_type TEXT NOT NULL,
    entity_id UUID NOT NULL,
    sync_status TEXT DEFAULT 'PENDING',
    retry_count INTEGER DEFAULT 0,
    queued_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    error_log TEXT
);

-- ==========================================
-- INDEXES
-- ==========================================
CREATE INDEX idx_subjects_user ON public.subjects(user_id);
CREATE INDEX idx_topics_subject ON public.topics(subject_id);
CREATE INDEX idx_topics_user ON public.topics(user_id);
CREATE INDEX idx_subtopics_topic ON public.subtopics(topic_id);
CREATE INDEX idx_revisions_user_date ON public.revisions(user_id, revision_date);
CREATE INDEX idx_focus_sessions_user_time ON public.focus_sessions(user_id, start_time);
CREATE INDEX idx_exams_user_date ON public.exams(user_id, exam_date);
CREATE INDEX idx_weak_topics_user_score ON public.weak_topics(user_id, weak_score);
CREATE INDEX idx_sync_queue_status ON public.sync_queue(user_id, sync_status);


-- ==========================================
-- ROW LEVEL SECURITY (RLS)
-- ==========================================

-- Enable RLS on all tables
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subjects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.topics ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subtopics ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.revisions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.focus_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.analytics ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.planner_data ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exams ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weak_topics ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_queue ENABLE ROW LEVEL SECURITY;

-- Create Policies

-- Users
CREATE POLICY "Users can only see and update their own profile"
ON public.users FOR ALL USING (auth.uid() = id);

-- Subjects
CREATE POLICY "Users can fully manage their own subjects"
ON public.subjects FOR ALL USING (auth.uid() = user_id);

-- Topics
CREATE POLICY "Users can fully manage their own topics"
ON public.topics FOR ALL USING (auth.uid() = user_id);

-- Subtopics
CREATE POLICY "Users can fully manage their own subtopics"
ON public.subtopics FOR ALL USING (auth.uid() = user_id);

-- Revisions
CREATE POLICY "Users can fully manage their own revisions"
ON public.revisions FOR ALL USING (auth.uid() = user_id);

-- Focus Sessions
CREATE POLICY "Users can fully manage their own focus sessions"
ON public.focus_sessions FOR ALL USING (auth.uid() = user_id);

-- Analytics
CREATE POLICY "Users can view and manage their own analytics"
ON public.analytics FOR ALL USING (auth.uid() = user_id);

-- Planner Data
CREATE POLICY "Users can manage their own planner data"
ON public.planner_data FOR ALL USING (auth.uid() = user_id);

-- App Preferences
CREATE POLICY "Users can fully manage their own preferences"
ON public.app_preferences FOR ALL USING (auth.uid() = user_id);

-- Exams
CREATE POLICY "Users can fully manage their own exams"
ON public.exams FOR ALL USING (auth.uid() = user_id);

-- Weak Topics
CREATE POLICY "Users can fully manage their own weak topics"
ON public.weak_topics FOR ALL USING (auth.uid() = user_id);

-- Sync Queue
CREATE POLICY "Users can fully manage their own sync queue"
ON public.sync_queue FOR ALL USING (auth.uid() = user_id);


CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_subjects
BEFORE UPDATE ON subjects
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_topics
BEFORE UPDATE ON topics
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_subtopics
BEFORE UPDATE ON subtopics
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_revisions
BEFORE UPDATE ON revisions
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_focus_sessions
BEFORE UPDATE ON focus_sessions
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_daily_plans
BEFORE UPDATE ON daily_plans
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_exams
BEFORE UPDATE ON exams
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_analytics
BEFORE UPDATE ON analytics
FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER set_updated_at_planner_data
BEFORE UPDATE ON planner_data
FOR EACH ROW EXECUTE FUNCTION update_updated_at();
