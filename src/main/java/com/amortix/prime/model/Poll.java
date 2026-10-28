package com.amortix.prime.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AGM Resolution and Poll Entity.
 * Corresponds to Amortix savePoll.
 */
@Entity
@Table(name = "society_polls")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    @Column(length = 2000)
    private String description;
    private LocalDateTime startDate = LocalDateTime.now();
    private LocalDateTime endDate = LocalDateTime.now().plusDays(7);
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_options", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "option_text")
    private List<String> options = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_vote_counts", joinColumns = @JoinColumn(name = "poll_id"))
    @MapKeyColumn(name = "option_key")
    @Column(name = "vote_count")
    private Map<String, Integer> voteCounts = new HashMap<>(); // Option -> Count

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_member_votes", joinColumns = @JoinColumn(name = "poll_id"))
    @MapKeyColumn(name = "member_code")
    @Column(name = "chosen_option")
    private Map<String, String> memberVotes = new HashMap<>(); // MemberCode -> Option Chosen

    private String createdBy;
    private LocalDateTime createdDate = LocalDateTime.now();

    public Poll() {}

    public Poll(String title, String description, List<String> options, String createdBy) {
        this.title = title;
        this.description = description;
        this.options = options;
        this.createdBy = createdBy;
        for (String opt : options) {
            voteCounts.put(opt, 0);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public Map<String, Integer> getVoteCounts() { return voteCounts; }
    public void setVoteCounts(Map<String, Integer> voteCounts) { this.voteCounts = voteCounts; }
    public Map<String, String> getMemberVotes() { return memberVotes; }
    public void setMemberVotes(Map<String, String> memberVotes) { this.memberVotes = memberVotes; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}
