package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransferControllerTest
{

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;



    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void transferAmountSuccess() throws Exception {

        String uniqueAccountId1 = "Id-123";
        String uniqueAccountId2 = "Id-124";
        Account account1 = new Account(uniqueAccountId1, new BigDecimal("15.0"));
        Account account2 = new Account(uniqueAccountId2, new BigDecimal("15.0"));
        this.accountsService.createAccount(account1);
        this.accountsService.createAccount(account2);

        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":5.0}")).andExpect(status().isOk());

        Account resultAccount1 = this.accountsService.getAccount(uniqueAccountId1);
        Account resultAccount2 = this.accountsService.getAccount(uniqueAccountId2);

        assertThat(resultAccount1.getBalance()).isEqualTo(new BigDecimal("10.0"));
        assertThat(resultAccount2.getBalance()).isEqualTo(new BigDecimal("20.0"));
    }

    @Test
    public void transferAmountFailedEmptyAccountFromId() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"\",\"accountToId\":\"Id-124\",\"amount\":5.0}")).andExpect(status().isBadRequest());
    }

    @Test
    public void transferAmountFailedNegativeTransferAmount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":-5.0}")).andExpect(status().isBadRequest());
    }

    @Test
    public void transferAmountFailedEmptyRequest() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void transferAmountFailedEmptyAccountToId() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"\",\"amount\":5.0}")).andExpect(status().isBadRequest());
    }

    @Test
    public void transferAmountFailedEmptyAmount() throws Exception {
        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-124\",\"amount\":}")).andExpect(status().isBadRequest());
    }


    @Test
    public void transferAmountFailedAccountNotExist() throws Exception {

        String uniqueAccountId1 = "Id-123";
        String uniqueAccountId2 = "Id-124";
        Account account1 = new Account(uniqueAccountId1, new BigDecimal("15.0"));
        Account account2 = new Account(uniqueAccountId2, new BigDecimal("15.0"));
        this.accountsService.createAccount(account1);
        this.accountsService.createAccount(account2);

        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-125\",\"amount\":5.0}")).andExpect(status().isConflict());

    }

    @Test
    public void transferAmountFailedInsufficientBalance() throws Exception {

        String uniqueAccountId1 = "Id-123";
        String uniqueAccountId2 = "Id-124";
        Account account1 = new Account(uniqueAccountId1, new BigDecimal("15.0"));
        Account account2 = new Account(uniqueAccountId2, new BigDecimal("15.0"));
        this.accountsService.createAccount(account1);
        this.accountsService.createAccount(account2);

        this.mockMvc.perform(post("/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountFromId\":\"Id-123\",\"accountToId\":\"Id-125\",\"amount\":50.0}")).andExpect(status().isConflict());

    }

}
