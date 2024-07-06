package com.accommodation_management_booking.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.accommodation_management_booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE (u.email LIKE %:keyword%)")
    List<User> searchByEmail(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE (u.email LIKE :keyword)")
    User searchUserByEmail(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE (u.userId = :id)")
    User searchUserById(@Param("id") int id);

    @Query("SELECT u FROM User u WHERE u.roleUser = 'EMPLOYEE' ")
    List<User> searchAllEmployees();


    //Dung cho tat ca role

    /**
     * Page<User> findAll(Pageable pageable);
     * Page<User> findByUsernameContaining(String username, Pageable pageable);
     * Page<User> findByEmailContaining(String email, Pageable pageable);
     * Page<User> findByPhoneNumberContaining(String phoneNumber, Pageable pageable);
     **/

    //Dung cho role cu the
    Page<User> findAllByRoleUser(User.Role role, Pageable pageable);

    Page<User> findByUsernameContainingAndRoleUser(String username, User.Role role, Pageable pageable);

    Page<User> findByEmailContainingAndRoleUser(String email, User.Role role, Pageable pageable);

    Page<User> findByPhoneNumberContainingAndRoleUser(String phoneNumber, User.Role role, Pageable pageable);

    Page<User> findByRoleUserAndUsernameContainingOrRoleUserAndEmailContainingOrRoleUserAndPhoneNumberContaining(
            User.Role role1, String keyword1,
            User.Role role2, String keyword2,
            User.Role role3, String keyword3,
            Pageable pageable);

    User findByUserId(Integer userId);
}
