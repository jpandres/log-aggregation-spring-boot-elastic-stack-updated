package com.example.commentservice;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@SpringBootApplication
public class CommentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommentServiceApplication.class, args);
	}

}

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Slf4j
class CommentController {


	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Comment>> getCommentsForPost(@RequestParam Long postId) {
		log.info("get comment for post: {}", postId);
		List<Comment> comments = List.of(new Comment(new Random().nextLong(), postId, "comment " + postId));
		return ResponseEntity.ok(comments);
	}
}


 record Comment (Long id, Long postId, String content) {}
