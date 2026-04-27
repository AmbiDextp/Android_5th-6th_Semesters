package com.example.cinematest.data

import com.example.cinematest.model.Question

val Questions: List<Question> = listOf(
    Question(
        id = 1,
        question = "Как зовут главного героя аниме и манги One Piece??",
        responses = listOf("Монги Д. Луси", "Монки Д. Луффи", "Манго Д. Луччи", "Минко Д. Лаффи"),
        correct = 1
    ),
    Question(
        id = 2,
        question = "Кто такой Тони-Тони Чоппер??",
        responses = listOf("Енот", "Пёс", "Олень", "Лис"),
        correct = 2
    ),
    Question(
        id = 3,
        question = "Как звали братьев Монки Д. Луффи?",
        responses = listOf("Коби и Хельмеппо", "Эйс и Сабо",
            "Ло и Беппо", "Зоро и Санджи"),
        correct = 1
    ),
    Question(
        id = 4,
        question = "Главная мечта навигатора команды Мугивар?",
        responses = listOf("Найти сокровища всего мира", "Уничтожить рыболюдей", "Найти Олл Блю", "Нарисовать карту мира"),
        correct = 3
    ),
    Question(
        id = 5,
        question = "Кто из списка не относился к Шичибукаям?",
        responses = listOf("Крокодайл", "Михоук", "Хэнкок", "Шанкс"),
        correct = 3
    ),
    Question(
        id = 6,
        question = "Кто из перечисленных не относиться к худшему поколению?",
        responses = listOf("Юстасс Кид", "Трафальгар Д. Ло",
            "Портгас Д. Эйс", "Маршалл Д. Тич"),
        correct = 2
    ),
    Question(
        id = 7,
        question = "У кого в подпольном мире есть прозвище 'Джокер'?",
        responses = listOf("Донкихот Дофламинго", "Капоне Бэдж",
            "Маршалл Д. Тич", "Кайдо"),
        correct = 0
    ),
    Question(
        id = 8,
        question = "Каким фруктом обладал Энель",
        responses = listOf("Гому-Гому", "Горо-Горо", "Опэ-Опэ", "Ями-Ями"),
        correct = 1
    ),
    Question(
        id = 9,
        question = "Самый разыскиваемый преступник?",
        responses = listOf("Монки Д. Драгон", "Монки Д. Луффи", "Гол Д. Роджер", "Эдвард Ньюгейт"),
        correct = 0
    ),
    Question(
        id = 10,
        question = "Как долго длилась беременность Портгас Ди Руж?(месяц)",
        responses = listOf("9", "20",
            "11", "18"),
        correct = 1
    )
)