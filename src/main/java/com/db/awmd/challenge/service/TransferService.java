package com.db.awmd.challenge.service;


import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotExistException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;

@Service
public class TransferService implements ITransferService
{
    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private ThreadExecutorService executorService;

    @Autowired
    private AccountsRepository accountsRepository;

    @Override
    public void transferAmount(Transfer transfer) throws InterruptedException
    {
        Account source = accountsRepository.getAccount(transfer.getAccountFromId());
        Account target = accountsRepository.getAccount(transfer.getAccountToId());
        if (source == null || target == null)
        {
            throw new AccountNotExistException("Account doesn't exist.");
        }
        synchronized (source){
            synchronized (target){
                //source operation
                BigDecimal remainingBalance = validateAngGetFinalAmount(source.getBalance(), transfer.getAmount());
                source.setBalance(remainingBalance);

                //targe operation
                BigDecimal targeFinalBalance = target.getBalance().add(transfer.getAmount());
                target.setBalance(targeFinalBalance);

                accountsRepository.updateAccount(source);
                accountsRepository.updateAccount(target);
            }
        }
        notifyRecipient(source, target);
    }

    private void notifyRecipient(final Account source, final Account target)
    {
        ExecutorService pool = executorService.getThreadPool();
        pool.submit(() -> {
            emailNotificationService.notifyAboutTransfer(source, "Your account has been debited please check your balance = " +source.toString());
            emailNotificationService.notifyAboutTransfer(target, "Your account has been credited please check your balance = " + target.toString());
        });
    }

    private BigDecimal validateAngGetFinalAmount(BigDecimal currentAmount, BigDecimal amountTobeDebited)
    {
        BigDecimal finalAmount = currentAmount.subtract(amountTobeDebited);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new InsufficientBalanceException("Transfer can't be completed due to lack of funds on the account.");
        }
        return finalAmount;
    }
}
