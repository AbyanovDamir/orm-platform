package ru.learning.platform.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.learning.platform.service.DirectoryService;

import java.util.List;

import static ru.learning.platform.service.PlatformData.*;

@RestController
@RequestMapping("/api")
public class DirectoryController {
    private final DirectoryService service;

    public DirectoryController(DirectoryService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public List<UserView> users() {
        return service.users();
    }

    @GetMapping("/users/{id}")
    public UserView user(@PathVariable @Positive Long id) {
        return service.user(id);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView createUser(@Valid @RequestBody UserInput input) {
        return service.createUser(input);
    }

    @PutMapping("/users/{id}")
    public UserView updateUser(@PathVariable @Positive Long id, @Valid @RequestBody UserInput input) {
        return service.updateUser(id, input);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long id) {
        service.deleteUser(id);
    }

    @GetMapping("/categories")
    public List<NamedView> categories() {
        return service.categories();
    }

    @GetMapping("/categories/{id}")
    public NamedView category(@PathVariable @Positive Long id) {
        return service.category(id);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public NamedView createCategory(@Valid @RequestBody CategoryInput input) {
        return service.createCategory(input);
    }

    @PutMapping("/categories/{id}")
    public NamedView updateCategory(@PathVariable @Positive Long id, @Valid @RequestBody CategoryInput input) {
        return service.updateCategory(id, input);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long id) {
        service.deleteCategory(id);
    }

    @GetMapping("/tags")
    public List<NamedView> tags() {
        return service.tags();
    }

    @GetMapping("/tags/{id}")
    public NamedView tag(@PathVariable @Positive Long id) {
        return service.tag(id);
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public NamedView createTag(@Valid @RequestBody TagInput input) {
        return service.createTag(input);
    }

    @PutMapping("/tags/{id}")
    public NamedView updateTag(@PathVariable @Positive Long id, @Valid @RequestBody TagInput input) {
        return service.updateTag(id, input);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable @Positive Long id) {
        service.deleteTag(id);
    }

    @GetMapping("/users/{id}/profile")
    public ProfileView profile(@PathVariable @Positive Long id) {
        return service.profile(id);
    }

    @PutMapping("/users/{id}/profile")
    public ProfileView saveProfile(@PathVariable @Positive Long id, @Valid @RequestBody ProfileInput input) {
        return service.saveProfile(id, input);
    }

    @DeleteMapping("/users/{id}/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable @Positive Long id) {
        service.deleteProfile(id);
    }
}
