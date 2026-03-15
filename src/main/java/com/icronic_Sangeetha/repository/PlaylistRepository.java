package com.icronic_Sangeetha.repository;

import com.icronic_Sangeetha.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Page<Playlist> findByIsPublicTrue(Pageable pageable);

    Page<Playlist> findByIsPublicTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}