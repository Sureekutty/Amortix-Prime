package com.amortix.prime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MemberDataIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Data Isolation: Member MEM-1001 CAN view their own member profile")
    void member_canViewOwnProfile() throws Exception {
        mockMvc.perform(get("/members/view/MEM-1001"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/view"))
                .andExpect(model().attributeExists("member"));
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Data Isolation: Member MEM-1001 CANNOT view other member MEM-1002's profile (403 Forbidden)")
    void member_cannotViewOtherMemberProfile() throws Exception {
        mockMvc.perform(get("/members/view/MEM-1002"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Data Isolation: Member MEM-1001 CAN view their own active loan")
    void member_canViewOwnLoan() throws Exception {
        mockMvc.perform(get("/loans/view/LTL-10001"))
                .andExpect(status().isOk())
                .andExpect(view().name("loans/view"))
                .andExpect(model().attributeExists("loan"));
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Data Isolation: Member MEM-1001 CANNOT view other member's deposit account (FXD-10001 - 403 Forbidden)")
    void member_cannotViewOtherMemberDeposit() throws Exception {
        mockMvc.perform(get("/deposits/view/FXD-10001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Data Isolation: Member MEM-1001 CANNOT view other member's thrift ledger (MEM-1002 - 403 Forbidden)")
    void member_cannotViewOtherMemberThrift() throws Exception {
        mockMvc.perform(get("/thrift/member/MEM-1002"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "STAFF"})
    @DisplayName("Admin has full authority to view all member profiles and accounts")
    void admin_canViewAnyMember() throws Exception {
        mockMvc.perform(get("/members/view/MEM-1001")).andExpect(status().isOk());
        mockMvc.perform(get("/members/view/MEM-1002")).andExpect(status().isOk());
        mockMvc.perform(get("/deposits/view/FXD-10001")).andExpect(status().isOk());
    }
}
