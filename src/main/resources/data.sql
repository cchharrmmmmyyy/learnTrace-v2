INSERT IGNORE INTO lt_article
(article_id, title, word_count, difficulty, content, questions)
values
    ('art_001', 'The History of Coffee', 1200, 'medium',
     '{"paragraphs": [{"paragraph_id": "p1", "sentences": [{"sentence_id": "p1_s1", "text": "Coffee was first discovered in Ethiopia.", "words": [{"word_id": "p1_s1_w1", "text": "Coffee"}, {"word_id": "p1_s1_w2", "text": "was"}]}]}]}',
     '[{"question_id": "q1", "type": "single_choice", "text": "Where was coffee first discovered?", "options": [{"option_id": "q1_a", "text": "Brazil"}, {"option_id": "q1_b", "text": "Ethiopia"}], "correct_answer": "q1_b", "explanation": "The passage states coffee was discovered in Ethiopia.", "sentence_ids": ["p1_s1"]}]'),
    ('art_002', 'The Solar System', 800, 'easy',
     '{"paragraphs": [{"paragraph_id": "p1", "sentences": [{"sentence_id": "p1_s1", "text": "The sun is at the center of our solar system.", "words": [{"word_id": "p1_s1_w1", "text": "sun"}]}]}]}',
     NULL),
    ('art_003', 'Broken Article', 500, 'medium',
     '{"paragraphs": "oops"}',
     NULL)
;

INSERT IGNORE INTO lt_theme
(theme_id, name)
VALUES
    ('theme_001', 'history'),
    ('theme_002', 'science');

INSERT IGNORE INTO lt_article_theme
(article_id, theme_id)
    VALUES
        ('art_001', 'theme_001'),
        ('art_002', 'theme_002');