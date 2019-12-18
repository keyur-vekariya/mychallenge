package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotExistException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.ITransferService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransferServiceTest
{
    @Autowired
    private AccountsService accountsService;

    @Autowired
    private ITransferService transferService;

    @Before
    public void prepareMockMvc() {
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void transferAmountSuccess() throws Exception
    {
        String accountId1 = "Id-123";
        String accountId2 = "Id-124";
        Account account1 = new Account(accountId1, new BigDecimal("15.0"));
        Account account2 = new Account(accountId2, new BigDecimal("15.0"));

        this.accountsService.createAccount(account1);
        this.accountsService.createAccount(account2);

        Transfer transfer = new Transfer(accountId1, accountId2, new BigDecimal("5.0"));
        this.transferService.transferAmount(transfer);

        Account resultAccount1 = this.accountsService.getAccount(accountId1);
        Account resultAccount2 = this.accountsService.getAccount(accountId2);

        assertThat(resultAccount1.getBalance()).isEqualTo(new BigDecimal("10.0"));
        assertThat(resultAccount2.getBalance()).isEqualTo(new BigDecimal("20.0"));
    }

   @Test
    public void transferAmountFailedInsufficientBalance() throws Exception
    {
        String accountId1 = "Id-123";
        String accountId2 = "Id-124";
        Account account1 = new Account(accountId1, new BigDecimal("15.0"));
        Account account2 = new Account(accountId2, new BigDecimal("15.0"));

        this.accountsService.createAccount(account1);
        this.accountsService.createAccount(account2);

        Transfer transfer = new Transfer(accountId1, accountId2, new BigDecimal("50.0"));
        try
        {
            this.transferService.transferAmount(transfer);
            fail("Should have failed when transfering amount");
        } catch (InsufficientBalanceException ex)
        {
            assertThat(ex.getMessage()).isEqualTo("Transfer can't be completed due to lack of funds on the account.");
        }
    }

     @Test
    public void transferAmountFailedAccountNotExist() throws Exception
    {
        String accountId1 = "Id-123";
        String accountId2 = "Id-124";
        Account account1 = new Account(accountId1, new BigDecimal("15.0"));
        Account account2 = new Account(accountId2, new BigDecimal("15.0"));

        this.accountsService.createAccount(account1);
        this.accountsService.createAccount(account2);

        Transfer transfer = new Transfer(accountId1, "Id-125", new BigDecimal("5.0"));
        try
        {
            this.transferService.transferAmount(transfer);
            fail("Should have failed when transfering amount");
        } catch (AccountNotExistException ex)
        {
            assertThat(ex.getMessage()).isEqualTo("Account doesn't exist.");
        }
    }
}
