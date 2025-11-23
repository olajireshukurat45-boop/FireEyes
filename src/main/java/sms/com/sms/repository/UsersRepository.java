package sms.com.sms.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import sms.com.sms.dto.UserDTO;
import sms.com.sms.model.Users;
@EnableJpaRepositories
public interface UsersRepository extends JpaRepository<Users, String> {

  boolean existsByPhonenumber(String phonenumber);
Optional<Users> findByPhonenumber(String phonenumber);
  // // @Query("SELECT u FROM Users u LEFT JOIN FETCH u.gasDetectors WHERE u.phoneNumber = :phoneNumber")
  // Users findByphoneNumberWithDetectors(@Param("phoneNumber") String phoneNumber);
Users findByEmail(String email);
Users findByEmailVerificationCode(String code);
Users findByResetToken(String token);
  

    // @EntityGraph(attributePaths = "gasDetectors")
    // Users findByphoneNumber(String phoneNumber);
    // // @Query("SELECT u FROM Users u LEFT JOIN FETCH u.gasDetectors ugd LEFT JOIN FETCH ugd.gasDetector WHERE u.phoneNumber = :phoneNumber")
    // Optional<Users> findWithGasDetectors(@Param("phone") String phone);
    
}