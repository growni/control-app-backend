package com.example.Backend.controllers;

import com.example.Backend.entities.Subscriber;
import com.example.Backend.repositories.SubscriberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SubscriberRepository subscriberRepository;
    private final JavaMailSender mailSender;

    @Value("${admin.api.key}")
    private String adminApiKey;

    public AdminController(SubscriberRepository subscriberRepository, JavaMailSender mailSender) {
        this.subscriberRepository = subscriberRepository;
        this.mailSender = mailSender;
    }

    @PostMapping("/send-patch-notes")
    public ResponseEntity<String> sendPatchNotes(@RequestHeader("X-API-KEY") String key, @RequestBody Map<String, String> request) {

        if(!adminApiKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized.");
        }

        String notes = request.get("notes");

        if(notes == null || notes.isBlank()) {
            return ResponseEntity.badRequest().body("Patch notes content is required.");
        }

        List<Subscriber> subscribers = subscriberRepository.findAll();

        int successfullySentCount = 0;

        for(Subscriber sub : subscribers) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setTo(sub.getEmail());
                helper.setSubject("Control App - Patch Notes");
                helper.setText("""
                    <h3>New Update Available!</h3>
                    <p>%s</p>
                    <p><a href="https://github.com/growni/control-app-JavaFX/releases" target="_blank">Download the new version here</a></p>
                    <p style="font-size: 12px;">If you no longer wish to receive these emails, you can unsubscribe from your profile page on the application.</p>
                    """.formatted(notes), true);

                mailSender.send(message);
                successfullySentCount++;

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok("Patch notes sent to %d subscribers.".formatted(successfullySentCount));
    }
}
