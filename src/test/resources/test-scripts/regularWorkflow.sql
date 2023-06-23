-- Create categories
insert into
    sanity_check_categories (id, name, email_to)
values
    (1, 'Person Data Irregularities', 'test1@email.com'),
    (2, 'Irregular Results', 'test1@email.com'),
    (3, 'User Data Irregularities', 'test2@email.com');

-- Insert sanity check
insert into
    sanity_checks (id, sanity_check_category_id, topic, query)
values
    (
        1,
        1,
        'Names with numbers',
        'SELECT * FROM Persons WHERE name REGEXP \'[0-9]\''
    ),
    (
        2,
        1,
        'Query with error',
        'CELECT * FROM Persons WHERE name REGEXP \'[0-9]\''
    ),
    (
        3,
        3,
        'Inconsistent name in users table',
        'SELECT p.id, p.name as profile_name, u.name as account_name FROM Persons p INNER JOIN users u ON p.id=u.wca_id AND p.name<>u.name AND p.subId=1'
    );

-- Insert competitors. There's one competitor with number
insert into
    wca_development.Persons (
        id,
        subId,
        name,
        countryId,
        gender,
        `year`,
        `month`,
        `day`,
        comments,
        rails_id,
        incorrect_wca_id_claim_count
    )
values
    (
        '1982BORS01',
        1,
        'Elsie-May Talbot',
        'Serbia',
        'm',
        1954,
        12,
        4,
        '',
        1,
        0
    ),
    (
        '1982BRIN01',
        1,
        'Gianni Lozano',
        'Germany',
        'm',
        1954,
        12,
        4,
        '',
        2,
        0
    ),
    (
        '1982CHIL01',
        1,
        'Saif Chandler',
        'United Kingdom',
        'm',
        1954,
        12,
        4,
        '',
        3,
        0
    ),
    (
        '1982FRID01',
        1,
        'Dottie Marsh',
        'USA',
        'f',
        1954,
        12,
        4,
        '',
        4,
        0
    ),
    (
        '1982FRID01',
        2,
        'Dottie Marsh 2',
        'Czech Republic',
        'f',
        1954,
        12,
        4,
        '',
        5,
        0
    ),
    (
        '1982GALR01',
        1,
        'Colleen Scott',
        'Portugal',
        'm',
        1954,
        12,
        4,
        '',
        6,
        0
    ),
    (
        '1982JEAN01',
        1,
        'Ellie-Mai Swift',
        'France',
        'm',
        1954,
        12,
        4,
        '',
        7,
        0
    ),
    (
        '1982LABA01',
        1,
        'Lilli Sims',
        'Hungary',
        'm',
        1954,
        12,
        4,
        '',
        8,
        0
    ),
    (
        '1982LAET01',
        1,
        'Ignacy Snider',
        'Belgium',
        'm',
        1954,
        12,
        4,
        '',
        9,
        0
    ),
    (
        '1982PETR01',
        1,
        'Lola-Rose Nelson',
        'Sweden',
        'm',
        1954,
        12,
        4,
        '',
        10,
        0
    );

-- names from https://www.name-generator.org.uk/quick/
INSERT INTO
    wca_development.users (
        id,
        email,
        encrypted_password,
        sign_in_count,
        current_sign_in_at,
        last_sign_in_at,
        confirmed_at,
        created_at,
        updated_at,
        name,
        region,
        wca_id,
        avatar,
        dob,
        gender,
        country_iso2,
        results_notifications_enabled,
        receive_delegate_reports,
        dummy_account,
        otp_required_for_login,
        cookies_acknowledged
    )
values
    (
        6713,
        '6713@worldcubeassociation.org',
        '',
        0,
        '2017-04-03 19:13:42',
        '2017-02-17 01:00:55',
        '2015-11-24 19:43:41',
        '2015-11-24 19:43:19',
        '2017-04-03 19:13:42',
        'Lola Rose Nelson',
        '',
        '1982PETR01',
        '1489600485.jpg',
        '1954-12-04',
        'm',
        'SE',
        0,
        0,
        0,
        0,
        0
    );