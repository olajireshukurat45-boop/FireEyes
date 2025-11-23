package sms.com.sms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import sms.com.sms.dto.DetectorDTO;
import sms.com.sms.dto.UserDTO;
import sms.com.sms.dto.UserGasDetectorDTO;
import sms.com.sms.enums.NotificationPreference;
import sms.com.sms.enums.UserRole;
import sms.com.sms.exception.ResourceNotFoundException;
import sms.com.sms.mapper.UserMapper;
import sms.com.sms.model.GasDetector;
import sms.com.sms.model.Users;
import sms.com.sms.repository.UsersRepository;
import sms.com.sms.repository.GasDetectorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class UserServiceImpl implements UserService {

    private final UsersRepository repository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private SmsService smsService;
    @Autowired 
     private GasDetectorRepository gasDetectorRepository ;
    @Autowired
    private MailService emailService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OTPService otpService;
    @Autowired
    private EmailVerificationService emailVerificationService;
    private final Map<String, Users> tempUserStorage = new HashMap<>();

    public UserServiceImpl(UsersRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isPhonenumberRegistered(String phonenumber) {
        return repository.existsById(phonenumber);
    }

    @Override
    public void saveTempUser(Users user) {
        tempUserStorage.put(user.getPhonenumber(), user);
    }
public Optional<UserGasDetectorDTO> getUserAndGasDetector(String phoneNumber, String macAddress) {
    if (!phoneNumber.startsWith("234")) {
        phoneNumber = "234" + phoneNumber.replaceFirst("^0", "");
    }

    Optional<Users> optionalUser = repository.findByPhonenumber(phoneNumber);
    GasDetector detector = gasDetectorRepository.findByMacAddress(macAddress);

    if (optionalUser.isPresent() && detector != null) {
        Users user = optionalUser.get();

        if (user.getGasDetectors().contains(detector)) {
            UserDTO userDTO = new UserDTO();
            userDTO.setPhoneNumbers(user.getPhonenumber());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setNotificationPreference(user.getNotificationPreference());
            userDTO.setmacAddress(
                user.getGasDetectors().stream()
                    .map(GasDetector::getMacAddress)
                    .collect(Collectors.toSet())
            );

            DetectorDTO gasDTO = new DetectorDTO();
            gasDTO.setMacAddress(detector.getMacAddress());
            gasDTO.setLocation(detector.getLocation());
            gasDTO.setStatus(detector.getStatus());
            gasDTO.setTemperature(detector.getTemperature());
            gasDTO.setHumidity(detector.getHumidity());
            gasDTO.setCo2(detector.getCo2());

            UserGasDetectorDTO result = new UserGasDetectorDTO();
            result.setUser(userDTO);
            result.setGasDetector(gasDTO);

            return Optional.of(result);
        }
    }

    return Optional.empty();
}

    @Override
    public Users findTempUser(String phonenumber) {
        return tempUserStorage.get(phonenumber);
    }

    public String sendOtp(String to) {
        boolean exists = isPhonenumberRegistered(to);
        if (exists) {

            throw new IllegalArgumentException("Phone number as been Used");
        }
        if (to == null) {

            throw new IllegalArgumentException("Phone number is required");
        }
        if (!to.startsWith("234")) {
            to = "234" + to.replaceFirst("^0", "");
        }
        String otp = otpService.generateOtp(to);
        String message = "Dear User your Verification Pin is " + otp
                + " Valid for 5 minutes, one-time use only.( Olawale say a big thank for everyone that participate in the testing case)";
        return smsService.sendSms(to, message);
    }

    public ResponseEntity<?> emailVerificationCode(String email) {
        Users user = repository.findByEmail(email);
        if (user != null) {
            return ResponseEntity.badRequest().body("Email has been used");
        } else {
            user = new Users(); // Ensure this matches your constructor logic
            user.setEmail(email);
            user.setEmailVerificationCode(UUID.randomUUID().toString());
            user.setEmailVerified(false);
            repository.save(user);

            String message = "Your email verification code is: " + user.getEmailVerificationCode();
            emailService.sendVerificationEmail(email, message);

            return ResponseEntity.ok("Verification code sent");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> verifyOtpAndCreateUser(Users user) {
        String phoneNumber = user.getPhonenumber();
        String inputOtp = user.getOtp();
        String email = user.getEmail();
        String emailOTP = user.getEmailVerificationCode();
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (!phoneNumber.startsWith("234")) {
            phoneNumber = "234" + phoneNumber.replaceFirst("^0", "");
        }

        if (inputOtp == null) {
            throw new IllegalArgumentException("OTP is required");
        }

        if (isPhonenumberRegistered(phoneNumber)) {
            return ResponseEntity.ok("Phone number already registered.");
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.ROLE_USER);
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        boolean valid = otpService.verifyOtp(phoneNumber, inputOtp);
        boolean emailValid = emailVerificationService.verifyOtp(email,emailOTP);
        if (valid) {
            user.setPhonenumber(phoneNumber);
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            if (emailValid) {

                user.setEmailVerified(true);
                user.setEmailVerificationCode(null);

                repository.save(user);

                return ResponseEntity.ok("User registered successfully.");
            } else {
                return ResponseEntity.badRequest().body("Check the  email verification code ,Verification failed");

            }
        } else {
            return ResponseEntity.badRequest().body("Failed to register user. OTP invalid.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = repository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = user.getRole().name().startsWith("ROLE_")
                ? user.getRole().name()
                : "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhonenumber())
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------

    public ResponseEntity<?> resetPassword(String token, String newPassword) {
        Users user = repository.findByResetToken(token);
        if (user == null)
            return ResponseEntity.badRequest().body("Invalid token");

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        repository.save(user);
        return ResponseEntity.ok("Password successfully reset");
    }

    // -------------------------------------------------------------------------------------------------------------
    public ResponseEntity<?> forgotPassword(String email) {
        Users user = repository.findByEmail(email);
        if (user == null)
            return ResponseEntity.badRequest().body("Email not found");

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        repository.save(user);

        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);

        return ResponseEntity.ok("Password reset link sent");
    }

    // -----------------------------------------------------------------
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable)
                .map(userMapper::toDto);
    }

    public Optional<UserDTO> getUserByPhone(String phone) {
         if (!phone.startsWith("234")) {
            phone = "234" + phone.replaceFirst("^0", "");
        }
        return repository.findById(phone)
                .map(userMapper::toDto);
    }

    public Optional<UserDTO> updateUser(String phone, UserDTO dto) {
        if (!phone.startsWith("234")) {
            phone = "234" + phone.replaceFirst("^0", "");
        }
        return repository.findById(phone).map(existing -> {
            existing.setName(dto.getName());
            existing.setEmail(dto.getEmail());
            if (dto.getNotificationPreference() != null) {
                existing.setNotificationPreference(dto.getNotificationPreference());
            }
            return userMapper.toDto(repository.save(existing));
        });
    }

    public String deleteUser(String phonenumber) {
        if (repository.existsById(phonenumber)) {
            repository.deleteById(phonenumber);
            return "Deleted Successfully";
        }
        return "Number not exist ";
    }
}
