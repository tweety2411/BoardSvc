package com.heypli.boardsvc.repository;

import com.heypli.boardsvc.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
    public Board findByTitle(String title);
}
