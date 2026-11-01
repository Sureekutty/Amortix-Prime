package com.amortix.prime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ==========================================
    // 1. UNAUTHENTICATED SCENARIOS
    // ==========================================

    @Test
    @DisplayName("Unauthenticated request to /dashboard must redirect to /login")
    void unauthenticated_toDashboard_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Unauthenticated request to /members must redirect to /login")
    void unauthenticated_toMembers_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Unauthenticated request to /admin/rules must redirect to /login")
    void unauthenticated_toAdminRules_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/rules"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ==========================================
    // 2. MEMBER ROLE SECURITY BOUNDARIES
    // ==========================================

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /admin/rules is forbidden (403)")
    void member_accessingAdminRules_isDenied() throws Exception {
        mockMvc.perform(get("/admin/rules"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /admin/users is forbidden (403)")
    void member_accessingAdminUsers_isDenied() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /accounting/daybook is forbidden (403)")
    void member_accessingAccountingDayBook_isDenied() throws Exception {
        mockMvc.perform(get("/accounting/daybook"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /recovery is forbidden (403)")
    void member_accessingMonthlyRecovery_isDenied() throws Exception {
        mockMvc.perform(get("/recovery"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /members portfolio list is forbidden (403)")
    void member_accessingMembersList_isDenied() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /deposits list is forbidden (403)")
    void member_accessingDepositsList_isDenied() throws Exception {
        mockMvc.perform(get("/deposits"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member submitting loan approval POST is forbidden (403)")
    void member_submittingLoanApproval_isDenied() throws Exception {
        mockMvc.perform(post("/loans/approve/LTL-10001").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member submitting loan disbursement POST is forbidden (403)")
    void member_submittingLoanDisbursement_isDenied() throws Exception {
        mockMvc.perform(post("/loans/disburse/LTL-10001").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member submitting member settlement execution POST is forbidden (403)")
    void member_submittingMemberSettlement_isDenied() throws Exception {
        mockMvc.perform(post("/members/settle/MEM-1001/execute").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /dashboard is permitted (Member Cockpit)")
    void member_accessingDashboard_isAllowed() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("isMemberPortal", true));
    }

    @Test
    @WithMockUser(username = "member", roles = {"MEMBER"})
    @DisplayName("Member accessing /polls is permitted for democratic voting")
    void member_accessingPolls_isAllowed() throws Exception {
        mockMvc.perform(get("/polls"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/polls"));
    }

    // ==========================================
    // 3. STAFF ROLE SECURITY BOUNDARIES
    // ==========================================

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Staff accessing /admin/rules is forbidden (403)")
    void staff_accessingAdminRules_isDenied() throws Exception {
        mockMvc.perform(get("/admin/rules"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Staff accessing /admin/users is forbidden (403)")
    void staff_accessingAdminUsers_isDenied() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Staff attempting to close daybook is forbidden (403 - Admin only)")
    void staff_closingDayBook_isDenied() throws Exception {
        mockMvc.perform(post("/accounting/daybook/close")
                        .param("date", "2026-10-31")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("Staff accessing operational modules is permitted")
    void staff_accessingOperations_isAllowed() throws Exception {
        mockMvc.perform(get("/members")).andExpect(status().isOk());
        mockMvc.perform(get("/loans")).andExpect(status().isOk());
        mockMvc.perform(get("/deposits")).andExpect(status().isOk());
        mockMvc.perform(get("/recovery")).andExpect(status().isOk());
        mockMvc.perform(get("/accounting/daybook")).andExpect(status().isOk());
    }

    // ==========================================
    // 4. ADMIN ROLE PRIVILEGES
    // ==========================================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "STAFF"})
    @DisplayName("Admin accessing admin governance pages is permitted")
    void admin_accessingGovernance_isAllowed() throws Exception {
        mockMvc.perform(get("/admin/rules")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/interest-rates")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/audit-logs")).andExpect(status().isOk());
    }
}
