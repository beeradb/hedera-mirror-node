package com.hedera.mirror.importer.parser.record;

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

import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;

import com.hedera.mirror.importer.FileCopier;
import com.hedera.mirror.importer.IntegrationTest;
import com.hedera.mirror.importer.domain.StreamType;

@Log4j2
@Tag("performance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecordFileParserPerformanceTest extends IntegrationTest {

    private static final String WARMUP_FILE = "2020-02-09T18_30_00.000084Z.rcd";

    @TempDir
    static Path dataPath;

    @Value("classpath:data")
    Path testPath;

    @Resource
    private RecordFilePoller recordFilePoller;

    @Resource
    private RecordParserProperties parserProperties;

    private FileCopier fileCopier;

    @BeforeAll
    void warmUp() throws Exception {
        parserProperties.getMirrorProperties().setDataPath(dataPath);
        parserProperties.init();
        parse(WARMUP_FILE, true);
    }

    @Timeout(30)
    @Test
    void parseAndIngestMultipleFiles60000Transactions() throws Exception {
        parse("*.rcd", false);
    }

    private void parse(String filePath, boolean warmup) throws Exception {
        StreamType streamType = parserProperties.getStreamType();
        fileCopier = FileCopier.create(testPath, dataPath)
                .from(streamType.getPath(), "performance")
                .filterFiles(filePath)
                .to(streamType.getPath(), streamType.getValid());
        fileCopier.copy();
        if (!warmup) {
            Files.deleteIfExists(fileCopier.getTo().resolve(WARMUP_FILE));
        }
        recordFilePoller.poll();
        Files.deleteIfExists(fileCopier.getTo().resolve(WARMUP_FILE));
    }
}
