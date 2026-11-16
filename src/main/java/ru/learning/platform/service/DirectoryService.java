package ru.learning.platform.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.platform.entity.*;
import ru.learning.platform.repository.*;

import java.util.List;
import java.util.Locale;

import static ru.learning.platform.service.PlatformData.*;
import static ru.learning.platform.service.PlatformException.*;

@Service
@Transactional(readOnly = true)
public class DirectoryService {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final TagRepository tags;
    private final ProfileRepository profiles;

    public DirectoryService(UserRepository users, CategoryRepository categories, TagRepository tags,
                            ProfileRepository profiles) {
        this.users = users;
        this.categories = categories;
        this.tags = tags;
        this.profiles = profiles;
    }

    public List<UserView> users() {
        return users.findAll(Sort.by("id")).stream().map(Views::user).toList();
    }

    public UserView user(Long id) {
        return Views.user(require(users.findById(id), "Пользователь", id));
    }

    @Transactional
    public UserView createUser(UserInput input) {
        return Views.user(users.save(new User(input.name().strip(), email(input), input.role())));
    }

    @Transactional
    public UserView updateUser(Long id, UserInput input) {
        User entity = require(users.findById(id), "Пользователь", id);
        check(entity.getRole() == input.role(), Kind.BAD_INPUT, "Роль задаётся при регистрации");
        entity.setName(input.name().strip());
        entity.setEmail(email(input));
        return Views.user(entity);
    }

    @Transactional
    public void deleteUser(Long id) {
        users.delete(require(users.findById(id), "Пользователь", id));
    }

    public List<NamedView> categories() {
        return categories.findAll(Sort.by("id")).stream().map(Views::category).toList();
    }

    public NamedView category(Long id) {
        return Views.category(require(categories.findById(id), "Категория", id));
    }

    @Transactional
    public NamedView createCategory(CategoryInput input) {
        return Views.category(categories.save(new Category(input.name().strip())));
    }

    @Transactional
    public NamedView updateCategory(Long id, CategoryInput input) {
        Category entity = require(categories.findById(id), "Категория", id);
        entity.setName(input.name().strip());
        return Views.category(entity);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categories.delete(require(categories.findById(id), "Категория", id));
    }

    public List<NamedView> tags() {
        return tags.findAll(Sort.by("id")).stream().map(Views::tag).toList();
    }

    public NamedView tag(Long id) {
        return Views.tag(require(tags.findById(id), "Тег", id));
    }

    @Transactional
    public NamedView createTag(TagInput input) {
        return Views.tag(tags.save(new Tag(input.name().strip())));
    }

    @Transactional
    public NamedView updateTag(Long id, TagInput input) {
        Tag entity = require(tags.findById(id), "Тег", id);
        entity.setName(input.name().strip());
        return Views.tag(entity);
    }

    @Transactional
    public void deleteTag(Long id) {
        tags.delete(require(tags.findById(id), "Тег", id));
    }

    public ProfileView profile(Long userId) {
        return Views.profile(require(profiles.findByUserId(userId), "Профиль пользователя", userId));
    }

    @Transactional
    public ProfileView saveProfile(Long userId, ProfileInput input) {
        User user = require(users.findById(userId), "Пользователь", userId);
        Profile profile = profiles.findByUserId(userId)
                .orElseGet(() -> new Profile(user, null, null));
        profile.setBio(input.bio());
        profile.setAvatarUrl(input.avatarUrl());
        return Views.profile(profiles.save(profile));
    }

    @Transactional
    public void deleteProfile(Long userId) {
        profiles.delete(require(profiles.findByUserId(userId), "Профиль пользователя", userId));
    }

    private static String email(UserInput input) {
        return input.email().strip().toLowerCase(Locale.ROOT);
    }
}
