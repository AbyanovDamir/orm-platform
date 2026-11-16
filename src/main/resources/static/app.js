"use strict";

const courseSelect = document.querySelector("#course");
const studentSelect = document.querySelector("#student");
const workspace = document.querySelector("#workspace");
const status = document.querySelector("#status");
let revision = 0;

function node(tag, content, className) {
    const element = document.createElement(tag);
    if (content !== undefined) element.textContent = content;
    if (className) element.className = className;
    return element;
}

async function api(path, method = "GET", data) {
    const response = await fetch("/api" + path, {
        method,
        headers: data === undefined ? undefined : {"Content-Type": "application/json"},
        body: data === undefined ? undefined : JSON.stringify(data)
    });
    if (response.status === 204) return;
    const result = await response.json();
    if (!response.ok) throw new Error(result.errors?.join(". ") ?? result.detail ?? "Ошибка запроса");
    return result;
}

function notice(message, error = false) {
    status.textContent = message;
    status.classList.toggle("error", error);
    status.hidden = !message;
}

function action(label, work, className = "") {
    const button = node("button", label, className);
    button.type = "button";
    button.addEventListener("click", async () => {
        button.disabled = true;
        try {
            await work();
        } catch (error) {
            notice(error.message, true);
        } finally {
            button.disabled = false;
        }
    });
    return button;
}

function select(selectElement, rows, text) {
    selectElement.replaceChildren(...rows.map(row => new Option(text(row), row.id)));
}

async function start() {
    const [courses, users] = await Promise.all([api("/courses"), api("/users")]);
    select(courseSelect, courses, course => course.title);
    select(studentSelect, users.filter(user => user.role === "STUDENT"), user => user.name);
    courseSelect.addEventListener("change", refresh);
    studentSelect.addEventListener("change", refresh);
    await refresh();
}

async function refresh() {
    const current = ++revision;
    const courseId = Number(courseSelect.value);
    const studentId = Number(studentSelect.value);
    if (!courseId || !studentId) {
        workspace.replaceChildren(node("p", "Создайте курс и студента через REST API или включите демоданные."));
        return;
    }
    workspace.replaceChildren(node("p", "Загружаем курс…", "muted"));
    try {
        const [detail, enrollments, submissions, attempts] = await Promise.all([
            api("/courses/" + courseId),
            api("/users/" + studentId + "/enrollments"),
            api("/users/" + studentId + "/submissions"),
            api("/users/" + studentId + "/quiz-attempts")
        ]);
        if (current !== revision) return;
        const enrollment = enrollments.find(entry => entry.courseId === courseId);
        const active = enrollment?.status === "ACTIVE";
        const overview = node("section");
        overview.append(node("h2", detail.course.title), node("p", detail.course.description));
        const tags = detail.course.tags.map(tag => tag.name).join(" · ");
        overview.append(node("p", [detail.course.duration, detail.course.startDate, tags].filter(Boolean).join(" · "), "muted"));
        const enrollmentPath = "/courses/" + courseId + "/enrollments/" + studentId;
        const actions = node("div", undefined, "actions");
        if (enrollment) {
            actions.append(node("span", active ? "Вы записаны" : "Курс завершён"),
                action("Отписаться", async () => {
                    await api(enrollmentPath, "DELETE");
                    await refresh();
                    notice("Запись удалена");
                }, "secondary"));
        } else {
            actions.append(action("Записаться на курс", async () => {
                await api(enrollmentPath, "POST");
                await refresh();
                notice("Вы записаны на курс");
            }));
        }
        overview.append(actions);
        workspace.replaceChildren(overview);
        const context = {studentId, teacherId: detail.course.teacherId, active, submissions, attempts};
        for (const item of detail.modules) workspace.append(renderModule(item, context));
    } catch (error) {
        if (current === revision) {
            workspace.replaceChildren();
            notice(error.message, true);
        }
    }
}

function renderModule(item, context) {
    const section = node("section");
    section.append(node("h3", item.module.orderIndex + ". " + item.module.title));
    if (item.module.description) section.append(node("p", item.module.description, "muted"));
    for (const detail of item.lessons) {
        const lesson = node("article");
        lesson.append(node("h4", detail.lesson.title), node("p", detail.lesson.content));
        for (const session of detail.sessions) {
            const time = new Date(session.startsAt).toLocaleString("ru-RU");
            lesson.append(node("p", "Занятие · " + time + " · " + session.location, "muted"));
        }
        for (const assignment of detail.assignments) lesson.append(renderAssignment(assignment, context));
        section.append(lesson);
    }
    if (item.quizId) {
        const quizPanel = node("article");
        const previous = context.attempts.find(attempt => attempt.quizId === item.quizId);
        if (previous) quizPanel.append(node("p", "Последняя попытка · " + previous.score + " / " + previous.maxScore));
        if (context.active) quizPanel.append(action("Открыть тест", async () => {
            renderQuiz(quizPanel, await api("/quizzes/" + item.quizId), context.studentId);
        }));
        section.append(quizPanel);
    }
    return section;
}

function renderAssignment(assignment, context) {
    const area = node("div");
    const work = context.submissions.find(entry => entry.assignmentId === assignment.id);
    area.append(node("p", "Задание · " + assignment.title), node("p", assignment.description),
        node("p", "Сдать до " + assignment.dueDate + " включительно (UTC)", "muted"));
    if (work) {
        area.append(node("p", work.score === null ? "Решение отправлено" :
            "Оценка · " + work.score + " / " + assignment.maxScore),
            node("p", work.content));
        if (work.feedback) area.append(node("p", work.feedback, "muted"));
        const grade = node("details", undefined, "grade");
        grade.append(node("summary", "Демо · выставить оценку"));
        const score = node("input");
        score.type = "number";
        score.min = "0";
        score.max = assignment.maxScore;
        score.setAttribute("aria-label", "Балл");
        const feedback = node("textarea");
        feedback.placeholder = "Комментарий преподавателя";
        feedback.setAttribute("aria-label", "Комментарий преподавателя");
        grade.append(score, feedback, action("Сохранить оценку", async () => {
            if (!score.value || !score.reportValidity()) throw new Error("Укажите балл в допустимом диапазоне");
            await api("/submissions/" + work.id + "/grade", "PUT", {
                teacherId: context.teacherId, score: Number(score.value), feedback: feedback.value
            });
            await refresh();
            notice("Оценка сохранена");
        }, "secondary"));
        area.append(grade);
    } else if (context.active) {
        const input = node("textarea");
        input.placeholder = "Текст решения";
        input.setAttribute("aria-label", "Текст решения для задания " + assignment.title);
        area.append(input, action("Отправить решение", async () => {
            await api("/assignments/" + assignment.id + "/submissions", "POST", {
                studentId: context.studentId, content: input.value
            });
            await refresh();
            notice("Решение отправлено");
        }));
    }
    return area;
}

function renderQuiz(panel, quiz, studentId) {
    const form = node("div");
    form.append(node("h4", quiz.title));
    if (quiz.timeLimit) form.append(node("p", "Рекомендуемое время · " + quiz.timeLimit + " мин.", "muted"));
    for (const question of quiz.questions) {
        const field = node("fieldset");
        field.append(node("legend", question.text));
        for (const option of question.options) {
            const input = node("input");
            input.type = question.type === "SINGLE_CHOICE" ? "radio" : "checkbox";
            input.name = String(question.id);
            input.value = option.id;
            const label = node("label");
            label.append(input, " " + option.text);
            field.append(label);
        }
        form.append(field);
    }
    form.append(action("Отправить ответы", async () => {
        const answers = Object.fromEntries(quiz.questions.map(question => [question.id, []]));
        for (const input of form.querySelectorAll("input:checked")) {
            answers[input.name].push(Number(input.value));
        }
        const attempt = await api("/quizzes/" + quiz.id + "/attempts", "POST", {studentId, answers});
        await refresh();
        notice("Результат теста · " + attempt.score + " / " + attempt.maxScore);
    }));
    panel.replaceChildren(form);
}

start().catch(error => notice(error.message, true));
