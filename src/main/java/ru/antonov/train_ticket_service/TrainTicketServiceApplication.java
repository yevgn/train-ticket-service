package ru.antonov.train_ticket_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.antonov.train_ticket_service.user.entity.Role;
import ru.antonov.train_ticket_service.user.entity.User;
import ru.antonov.train_ticket_service.user.repository.UserRepository;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class TrainTicketServiceApplication implements CommandLineRunner {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(TrainTicketServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		createAdmin();

	}
	public void createAdmin(){
		if(!userRepository.existsByEmail("example@gmail.com")) {
			User user = User.builder()
					.name("Елена")
					.surname("Фонталина")
					.patronymic("Сергеевна")
					.email("example@gmail.com")
					.role(Role.ADMIN)
					.password(passwordEncoder.encode("12345678"))
					.build();
			userRepository.save(user);
			log.info("Админ {} был добавлен", user);
		}
	}
}
