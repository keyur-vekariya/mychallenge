package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Transfer;

public interface ITransferService
{

    void transferAmount(Transfer transfer) throws InterruptedException;
}
