package com.example.data

object BundledContent {
  const val BLOG_BASE_URL = "https://gyanpetika.blogspot.com"
  const val FEED_URL =
    "https://gyanpetika.blogspot.com/feeds/posts/default?alt=json&max-results=500&orderby=published"

  fun feedPageUrl(startIndex: Int, maxResults: Int = 150): String =
    "https://gyanpetika.blogspot.com/feeds/posts/default?alt=json&max-results=$maxResults&start-index=$startIndex&orderby=published"

  val classFilterPills =
    listOf(
      "All",
      "Study Material",
      "Question Papers",
      "Current Affairs",
    )

  val searchFilterPills =
    listOf(
      "All",
      "Class 9",
      "Class 10",
      "Class 11",
      "Class 12",
      "Study Material",
      "Question Papers",
      "Current Affairs",
    )

  val exploreByClass =
    listOf(
      ClassHubInfo(
        id = "class-9",
        className = "Class 9",
        defaultBadge = "Foundation",
        subtitle = "Secondary Curriculum",
      ),
      ClassHubInfo(
        id = "class-10",
        className = "Class 10",
        defaultBadge = "Board Prep",
        subtitle = "Board Exam Preparation",
      ),
      ClassHubInfo(
        id = "class-11",
        className = "Class 11",
        defaultBadge = "Senior 11th",
        subtitle = "Science, Commerce & Arts",
      ),
      ClassHubInfo(
        id = "class-12",
        className = "Class 12",
        defaultBadge = "Board 12th",
        subtitle = "Science, Commerce & Arts",
      ),
    )

  val secondarySubjects =
    listOf(
      SubjectItem("mathematics", "Mathematics", SubjectAccent.BLUE),
      SubjectItem("science", "Science", SubjectAccent.GREEN),
      SubjectItem("english", "English", SubjectAccent.RED),
      SubjectItem("hindi", "Hindi", SubjectAccent.ORANGE),
      SubjectItem("sanskrit", "Sanskrit", SubjectAccent.ORANGE),
      SubjectItem("social-science", "Social Science", SubjectAccent.PURPLE),
      SubjectItem("information-technology", "Information Technology", SubjectAccent.TEAL),
      SubjectItem("computer-science", "Computer Science", SubjectAccent.BLUE),
    )

  val seniorSubjects =
    listOf(
      SubjectItem("physics", "Physics", SubjectAccent.TEAL),
      SubjectItem("chemistry", "Chemistry", SubjectAccent.GREEN),
      SubjectItem("mathematics", "Mathematics", SubjectAccent.BLUE),
      SubjectItem("biology", "Biology", SubjectAccent.GREEN),
      SubjectItem("accountancy", "Accountancy", SubjectAccent.ORANGE),
      SubjectItem("business-studies", "Business Studies", SubjectAccent.BLUE),
      SubjectItem("economics", "Economics", SubjectAccent.PURPLE),
      SubjectItem("history", "History", SubjectAccent.ORANGE),
      SubjectItem("political-science", "Political Science", SubjectAccent.PURPLE),
      SubjectItem("geography", "Geography", SubjectAccent.TEAL),
      SubjectItem("psychology", "Psychology", SubjectAccent.RED),
      SubjectItem("sociology", "Sociology", SubjectAccent.BLUE),
      SubjectItem("english", "English", SubjectAccent.RED),
      SubjectItem("information-technology", "Information Technology", SubjectAccent.TEAL),
      SubjectItem("computer-science", "Computer Science", SubjectAccent.BLUE),
    )

  fun subjectsForClass(className: String): List<SubjectItem> =
    if (className == "Class 11" || className == "Class 12") seniorSubjects else secondarySubjects

  val studyMaterialCategories =
    listOf(
      StudyMaterialCategory("chapter-notes", "Chapter-wise Notes"),
      StudyMaterialCategory("ncert-solutions", "NCERT Book and Solutions"),
      StudyMaterialCategory("important-questions", "Important Questions"),
      StudyMaterialCategory("pyq", "Previous Year Question Papers"),
      StudyMaterialCategory("mcq-practice", "MCQs Practice"),
    )

  val fallbackPosts: List<StudyPost> =
    listOf(
      StudyPost(
        id = "bundled-sample-entrepreneurship",
        title = "Entrepreneurship Skill– II SESSION : 1 Entrepreneurship and its impact",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "CBSE Class 10 IT (402)",
        subLabel = "Part A: Unit 4 • Session 1",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 5, 2026",
        fullPublishedDate = "Thursday, November 5, 2026",
        estimatedReadTime = "11 min read",
        snippet =
          "Comprehensive master notes on Entrepreneurship and its impact on society, economy, job creation, innovation, and individual growth with solved MCQs.",
        banner =
          ArticleBanner(
            eyebrow = "CBSE CLASS 10 IT • SUBJECT CODE 402",
            headline = "Entrepreneurship and Its Impact",
            subheadline = "Unit 4: Entrepreneurial Skills — Session 1",
            summary =
              "Comprehensive Master Notes, Societal & Economic Impact, Individual Benefits & Question Bank",
          ),
        htmlContent =
          """
          <div class="gp-section" data-num="01">
            <h2>Introduction to Entrepreneurship</h2>
            <p>Over the past few decades, our lifestyle and standard of living have changed tremendously. Modern appliances, rapid transport, digital communication, and automated services make everyday life easier. Behind every innovation stands an <strong>entrepreneur</strong> who identifies unmet needs, assumes calculated risk, and organizes resources to deliver practical value.</p>
            <ul>
              <li><strong>Meaning:</strong> Entrepreneurship is the process of designing, launching, and running a new business venture to solve real-world problems while generating sustainable value.</li>
              <li><strong>Core Spirit:</strong> Combines creativity, initiative, perseverance, and responsible risk-taking.</li>
            </ul>
          </div>
          <div class="gp-section" data-num="02">
            <h2>Positive Impact of Entrepreneurship on Society</h2>
            <p>Entrepreneurs act as catalysts of social and economic transformation across urban and rural communities:</p>
            <ul>
              <li><strong>Employment Generation:</strong> New enterprises create direct and indirect jobs for skilled and semi-skilled workers.</li>
              <li><strong>Economic Growth &amp; Wealth Sharing:</strong> Higher productivity boosts GDP, local incomes, and regional infrastructure.</li>
              <li><strong>Innovation &amp; Better Standard of Living:</strong> Affordable products, healthcare tools, and EdTech platforms improve daily life.</li>
              <li><strong>Balanced Regional Development:</strong> Small and medium enterprises empower Tier-2 and Tier-3 towns.</li>
            </ul>
          </div>
          <div class="gp-section" data-num="03">
            <h2>Individual Benefits &amp; Key Qualities</h2>
            <p>Choosing an entrepreneurial mindset builds independence, financial literacy, decision-making confidence, and leadership skills essential for 21st-century careers.</p>
          </div>
          <div class="gp-section" data-num="04">
            <h2>Practice Question Bank (MCQs)</h2>
            <p class="mcq-q" style="color:#D32F2F;">Q1. Which of the following best describes an entrepreneur?</p>
            <p>(a) Someone who avoids all risks &nbsp; (b) A person who identifies an opportunity, takes calculated risk, and builds value &nbsp; (c) Only a government officer &nbsp; (d) A passive investor</p>
            <p class="mcq-a" style="color:#2E7D32;">Answer: (b) A person who identifies an opportunity, takes calculated risk, and builds value</p>
            <p class="mcq-q" style="color:#D32F2F;">Q2. How does entrepreneurship primarily help local communities?</p>
            <p>(a) By reducing job opportunities &nbsp; (b) By creating employment and improving standard of living &nbsp; (c) By stopping innovation &nbsp; (d) By limiting product choices</p>
            <p class="mcq-a" style="color:#2E7D32;">Answer: (b) By creating employment and improving standard of living</p>
            <p class="mcq-q" style="color:#D32F2F;">Q3. Which skill is essential for sustaining a new enterprise?</p>
            <p>(a) Procrastination &nbsp; (b) Decision-making and problem-solving &nbsp; (c) Ignoring customer feedback &nbsp; (d) Avoiding planning</p>
            <p class="mcq-a" style="color:#2E7D32;">Answer: (b) Decision-making and problem-solving</p>
          </div>
          """
            .trimIndent(),
        postUrl = "https://gyanpetika.blogspot.com",
        labels =
          listOf(
            "Class 10",
            "Information Technology",
            "Class 10 Information Technology Chapter-wise Notes",
            "Class 10 Information Technology MCQs Practice",
          ),
        isBundled = true,
      ),
      StudyPost(
        id = "bundled-part-b-unit-4",
        title = "Part B — Unit 4: Maintain Health, Safety and Secure Working Environment",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "CBSE Class 10 IT (402)",
        subLabel = "Part B: Unit 4",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 13, 2026",
        fullPublishedDate = "Friday, November 13, 2026",
        estimatedReadTime = "9 min read",
        snippet =
          "Workplace health, ergonomics, electrical and fire safety protocols, emergency evacuation procedures, and cybersecurity hygiene for Class 10 IT (402).",
        banner =
          ArticleBanner(
            eyebrow = "CBSE CLASS 10 IT • SUBJECT CODE 402",
            headline = "Maintain Health, Safety & Secure Working Environment",
            subheadline = "Part B — Unit 4 Complete Revision Notes",
            summary = "Workplace Ergonomics, Hazard Prevention, First Aid & Emergency Response",
          ),
        htmlContent =
          """
          <div class="gp-section" data-num="01">
            <h2>Workplace Health &amp; Ergonomics</h2>
            <p>A safe working environment prevents physical strain, repetitive stress injuries, and accidents. Proper posture, eye-level monitor placement, and scheduled short breaks protect long-term well-being while using computers.</p>
            <ul>
              <li><strong>Monitor Distance:</strong> Keep the screen 50–70 cm away with the top edge at eye level.</li>
              <li><strong>Seating Posture:</strong> Feet flat on the floor, lower back supported, wrists neutral.</li>
              <li><strong>20-20-20 Eye Rule:</strong> Every 20 minutes, look at an object 20 feet away for 20 seconds.</li>
            </ul>
          </div>
          <div class="gp-section" data-num="02">
            <h2>Workplace Hazards &amp; Fire Safety</h2>
            <p>Hazards include electrical overload, wet floors, cluttered walkways, and poorly ventilated server rooms. Always inspect cables, keep emergency exits unobstructed, and know fire extinguisher classes (Class A, B, C, and Electrical CO₂).</p>
          </div>
          <div class="gp-section" data-num="03">
            <h2>Practice MCQs</h2>
            <p class="mcq-q" style="color:#D32F2F;">Q1. Which fire extinguisher is safest for live electrical equipment fires?</p>
            <p>(a) Water extinguisher &nbsp; (b) CO₂ / Dry chemical extinguisher &nbsp; (c) Foam only &nbsp; (d) Wet blanket</p>
            <p class="mcq-a" style="color:#2E7D32;">Answer: (b) CO₂ / Dry chemical extinguisher</p>
            <p class="mcq-q" style="color:#D32F2F;">Q2. Ergonomics primarily focuses on:</p>
            <p>(a) Designing workspaces to fit human comfort and safety &nbsp; (b) Increasing screen glare &nbsp; (c) Removing breaks &nbsp; (d) Overloading power sockets</p>
            <p class="mcq-a" style="color:#2E7D32;">Answer: (a) Designing workspaces to fit human comfort and safety</p>
          </div>
          """
            .trimIndent(),
        postUrl = "https://gyanpetika.blogspot.com",
        labels =
          listOf(
            "Class 10",
            "Information Technology",
            "Class 10 Information Technology Chapter-wise Notes",
            "Class 10 Information Technology Important Questions",
          ),
        isBundled = true,
      ),
      StudyPost(
        id = "bundled-part-b-unit-3",
        title = "Part B — Unit 3: Database Management System",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "CBSE Class 10 IT (402)",
        subLabel = "Part B: Unit 3",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 13, 2026",
        fullPublishedDate = "Friday, November 13, 2026",
        estimatedReadTime = "10 min read",
        snippet =
          "Relational Database Management System (RDBMS), tables, primary and foreign keys, SQL queries, forms, and reports in LibreOffice Base.",
        banner =
          ArticleBanner(
            eyebrow = "CBSE CLASS 10 IT • SUBJECT CODE 402",
            headline = "Database Management System (RDBMS)",
            subheadline = "Part B — Unit 3 Master Study Guide",
            summary = "Tables, Keys, Relationships, SQL Commands, Forms & Reports",
          ),
        htmlContent =
          """
          <div class="gp-section" data-num="01">
            <h2>Core Concepts of DBMS &amp; RDBMS</h2>
            <p>A <strong>Database Management System (DBMS)</strong> stores, organizes, and retrieves structured data efficiently while reducing data redundancy and inconsistency.</p>
            <ul>
              <li><strong>Table:</strong> Collection of related records arranged in rows (tuples) and columns (fields/attributes).</li>
              <li><strong>Primary Key:</strong> A unique field that identifies every record in a table without NULL values.</li>
              <li><strong>Foreign Key:</strong> Links a field in one table to the Primary Key of another table to establish relationships.</li>
            </ul>
          </div>
          <div class="gp-section" data-num="02">
            <h2>Essential SQL Commands</h2>
            <p>Use <strong>DDL</strong> commands (CREATE, ALTER, DROP) to define structure and <strong>DML</strong> commands (SELECT, INSERT, UPDATE, DELETE) to query and manipulate records.</p>
          </div>
          <div class="gp-section" data-num="03">
            <h2>Quick Revision MCQs</h2>
            <p class="mcq-q" style="color:#D32F2F;">Q1. Which key uniquely identifies each row in a relational database table?</p>
            <p>(a) Alternate Key &nbsp; (b) Primary Key &nbsp; (c) Foreign Key &nbsp; (d) Composite Key</p>
            <p class="mcq-a" style="color:#2E7D32;">Answer: (b) Primary Key</p>
          </div>
          """
            .trimIndent(),
        postUrl = "https://gyanpetika.blogspot.com",
        labels =
          listOf(
            "Class 10",
            "Information Technology",
            "Class 10 Information Technology Chapter-wise Notes",
            "Class 10 Information Technology MCQs Practice",
          ),
        isBundled = true,
      ),
      StudyPost(
        id = "bundled-part-b-unit-2",
        title = "Part B — Unit 2: Electronic Spreadsheet (Advanced)",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "CBSE Class 10 IT (402)",
        subLabel = "Part B: Unit 2",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 13, 2026",
        fullPublishedDate = "Friday, November 13, 2026",
        estimatedReadTime = "8 min read",
        snippet =
          "Data consolidation, subtotals, What-If scenarios, Goal Seek, Solver, linking sheets, and macros in LibreOffice Calc.",
        banner =
          ArticleBanner(
            eyebrow = "CBSE CLASS 10 IT • SUBJECT CODE 402",
            headline = "Electronic Spreadsheet (Advanced)",
            subheadline = "Part B — Unit 2 Study Notes",
            summary = "Consolidate, Subtotals, What-If Analysis, Goal Seek, Macros & Cell Referencing",
          ),
        htmlContent =
          """
          <div class="gp-section" data-num="01">
            <h2>Analyzing Data in Calc</h2>
            <p>Advanced spreadsheet tools automate calculations across multiple worksheets and support data-driven forecasting.</p>
            <ul>
              <li><strong>Consolidate:</strong> Combines data from separate ranges or sheets into a single summary sheet.</li>
              <li><strong>Subtotals:</strong> Automatically groups sorted data and inserts SUM, AVERAGE, or COUNT summaries.</li>
              <li><strong>Goal Seek:</strong> Finds the exact input value required to achieve a known target formula output.</li>
            </ul>
          </div>
          <div class="gp-section" data-num="02">
            <h2>Macros &amp; Sheet Sharing</h2>
            <p>A <strong>Macro</strong> records a sequence of repetitive commands so they can be replayed with one click, saving time and preventing manual entry mistakes.</p>
          </div>
          """
            .trimIndent(),
        postUrl = "https://gyanpetika.blogspot.com",
        labels =
          listOf(
            "Class 10",
            "Information Technology",
            "Class 10 Information Technology Chapter-wise Notes",
          ),
        isBundled = true,
      ),
      StudyPost(
        id = "bundled-part-b-unit-1",
        title = "Part B — Unit 1: Digital Documentation (Advanced)",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "CBSE Class 10 IT (402)",
        subLabel = "Part B: Unit 1 Digital Documentation",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 13, 2026",
        fullPublishedDate = "Friday, November 13, 2026",
        estimatedReadTime = "9 min read",
        snippet =
          "Styles and Formatting, working with images, document templates, Table of Contents, and Mail Merge in LibreOffice Writer.",
        banner =
          ArticleBanner(
            eyebrow = "CBSE CLASS 10 IT • SUBJECT CODE 402",
            headline = "Digital Documentation (Advanced)",
            subheadline = "Part B — Unit 1 Complete Notes",
            summary = "Styles, Templates, Images, Table of Contents & Track Changes",
          ),
        htmlContent =
          """
          <div class="gp-section" data-num="01">
            <h2>Using Styles in LibreOffice Writer</h2>
            <p>A <strong>Style</strong> is a named collection of formatting attributes (font, size, spacing, borders) that ensures visual consistency across long documents.</p>
            <ul>
              <li><strong>Paragraph &amp; Character Styles:</strong> Control block alignment vs. inline text emphasis.</li>
              <li><strong>Page &amp; Frame Styles:</strong> Manage margins, headers, footers, and image/frame wrapping.</li>
              <li><strong>Fill Format Mode:</strong> Rapidly applies a chosen style to multiple non-adjacent selections.</li>
            </ul>
          </div>
          <div class="gp-section" data-num="02">
            <h2>Templates &amp; Table of Contents</h2>
            <p>Templates store reusable layout blueprints (<code>.ott</code>), while an automated <strong>Table of Contents (ToC)</strong> builds clickable chapter indexes from Heading 1–3 styles.</p>
          </div>
          """
            .trimIndent(),
        postUrl = "https://gyanpetika.blogspot.com",
        labels =
          listOf(
            "Class 10",
            "Information Technology",
            "Class 10 Information Technology Chapter-wise Notes",
            "Class 10 Information Technology NCERT Book and Solutions",
          ),
        isBundled = true,
      ),
      StudyPost(
        id = "bundled-part-a-employability",
        title = "Part - A : Employability skills - II",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "CBSE Class 10 IT (402)",
        subLabel = "Part A",
        materialCategory = "NCERT Book and Solutions",
        publishedDate = "Nov 13, 2026",
        fullPublishedDate = "Friday, November 13, 2026",
        estimatedReadTime = "12 min read",
        snippet =
          "Complete Part A Employability Skills Class 10 notes covering Communication Skills, Self-Management, ICT Skills, Entrepreneurial Skills, and Green Skills.",
        banner =
          ArticleBanner(
            eyebrow = "CBSE CLASS 10 IT • SUBJECT CODE 402",
            headline = "Employability Skills — II",
            subheadline = "Part A Complete Syllabus Summary & Solutions",
            summary = "Communication, Self-Management, Basic ICT, Entrepreneurship & Green Skills",
          ),
        htmlContent =
          """
          <div class="gp-section" data-num="01">
            <h2>Unit 1: Communication Skills – II</h2>
            <p>Effective communication requires clarity, conciseness, active listening, and awareness of verbal, non-verbal, and visual cues. Overcoming language, physical, and cultural barriers improves workplace collaboration.</p>
          </div>
          <div class="gp-section" data-num="02">
            <h2>Unit 2: Self-Management &amp; Stress Management</h2>
            <p>Self-awareness, time planning, physical exercise, and positive mindset help students manage exam stress and build independent learning habits.</p>
          </div>
          <div class="gp-section" data-num="03">
            <h2>Unit 3–5: ICT, Entrepreneurial &amp; Green Skills</h2>
            <p>Covers operating systems, file security, sustainable development goals (SDGs), and green economy careers.</p>
          </div>
          """
            .trimIndent(),
        postUrl = "https://gyanpetika.blogspot.com",
        labels =
          listOf(
            "Class 10",
            "Information Technology",
            "Class 10 Information Technology NCERT Book and Solutions",
            "Class 10 Information Technology Previous Year Question Papers",
          ),
        isBundled = true,
      ),
    )
}
