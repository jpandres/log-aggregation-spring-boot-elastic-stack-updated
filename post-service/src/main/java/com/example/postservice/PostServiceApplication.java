package com.example.postservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SpringBootApplication
public class PostServiceApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }

}

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Slf4j
class PostController {
    private final PostService service;
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Post>> getPosts() {
        log.info("Finding details of all posts");
        return ResponseEntity.ok(service.getPosts());
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostWithComments> getPost(@PathVariable Long id) {
        var postWithComments = service.getPost(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(postWithComments);
    }
}

@ResponseStatus(
        code = HttpStatus.NOT_FOUND,
        reason = "Resource not found with the given identifier")
 class ResourceNotFoundException extends RuntimeException {

}
@Slf4j
@Service
@RequiredArgsConstructor
class PostService {

    @Value("${comment-service.base-url}")
    private String commentServiceBaseUrl;

    private final RestTemplate restTemplate;

    private static final List<Post> POSTS = List.of(
            new Post(1L, "Post 1", "Post 1 content", OffsetDateTime.now(ZoneOffset.UTC)),
            new Post(2L, "Post 2", "Post 2 content", OffsetDateTime.now(ZoneOffset.UTC)),
            new Post(3L, "Post 3", "Post 3 content", OffsetDateTime.now(ZoneOffset.UTC))
    );


    public List<Post> getPosts() {
        log.info("Finding details of all posts");
        return POSTS;
    }

    public Optional<PostWithComments> getPost(Long id) {

        log.info("Finding details of post with id {}", id);

        return  POSTS.stream()
                .filter(post -> post.id().equals(id))
                .findFirst()
                .map(p -> new PostWithComments( p.id(), p.title(), p.content(), p.publishDateTime(), this.findCommentsForPost(p)));
    }

    private List<Comment> findCommentsForPost(Post post) {

        log.info("Finding comments of post with id {}", post.id());

        String url = UriComponentsBuilder.fromHttpUrl(commentServiceBaseUrl)
                .path("comments")
                .queryParam("postId", post.id())
                .toUriString();

        ResponseEntity<List<Comment>> response = restTemplate.exchange(url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        List<Comment> comments = Objects.isNull(response.getBody()) ? List.of() : response.getBody();
        log.info("Found {} comment(s) of post with id {}", comments.size(), post.id());
        return comments;
    }
}


record Post(Long id, String title, String content, OffsetDateTime publishDateTime) {
}

record PostWithComments(Long id, String title, String content, OffsetDateTime publishDateTime, List<Comment> comments) {
}

record Comment(Long id, String content) {
}