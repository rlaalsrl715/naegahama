package com.hanghae.naegahama.repository.searcgrepository;

import com.hanghae.naegahama.domain.Search;
import com.hanghae.naegahama.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SearchRepository extends JpaRepository<Search, Long> {

    //boolean existsBySearchWordAndUser(String searchWord, User user);

    //List<Search> findAllByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);

    void deleteByIdAndUser(Long searchId, User user);
}
