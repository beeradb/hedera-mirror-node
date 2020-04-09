package com.hedera.mirror.importer.downloader.balance;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import java.io.File;
import javax.inject.Named;

import com.hedera.mirror.importer.util.Utility;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import com.hedera.mirror.importer.addressbook.NetworkAddressBook;
import com.hedera.mirror.importer.domain.ApplicationStatusCode;
import com.hedera.mirror.importer.downloader.Downloader;
import com.hedera.mirror.importer.leader.Leader;
import com.hedera.mirror.importer.repository.ApplicationStatusRepository;

@Log4j2
@Named
public class AccountBalancesDownloader extends Downloader {

    public AccountBalancesDownloader(
            S3AsyncClient s3Client, ApplicationStatusRepository applicationStatusRepository,
            NetworkAddressBook networkAddressBook, BalanceDownloaderProperties downloaderProperties) {
        super(s3Client, applicationStatusRepository, networkAddressBook, downloaderProperties);
    }

    @Leader
    @Override
    @Scheduled(fixedRateString = "${hedera.mirror.downloader.balance.frequency:30000}")
    public void download() {
        downloadNextBatch();
    }

    @Override
    protected boolean verifyHashChain(File file) {
        return true;
    }

    @Override
    protected ApplicationStatusCode getLastValidDownloadedFileKey() {
        return ApplicationStatusCode.LAST_VALID_DOWNLOADED_BALANCE_FILE;
    }

    @Override
    protected ApplicationStatusCode getLastValidDownloadedFileHashKey() {
        return null;
    }

    @Override
    protected ApplicationStatusCode getBypassHashKey() {
        return null;
    }

    @Override
    protected String getPrevFileHash(String filePath) {
        return null;
    }

    @Override
    protected byte[] getDataFileHash(String fileName) {
        return Utility.getBalanceFileHash(fileName);
    }
}
