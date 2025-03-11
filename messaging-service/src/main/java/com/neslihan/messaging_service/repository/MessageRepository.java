package com.neslihan.messaging_service.repository;

import com.neslihan.messaging_service.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
            String sender1, String receiver1, String sender2, String receiver2);
    @Query("SELECT DISTINCT m.sender FROM MessageEntity m WHERE m.receiver = :user " +
            "UNION " +
            "SELECT DISTINCT m.receiver FROM MessageEntity m WHERE m.sender = :user")
    List<String> findDistinctChatPartners(@Param("user") String user);
}
