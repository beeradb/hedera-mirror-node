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
'use strict';

const transactions = require('../transactions.js');

function normalizeSql(str) {
  return str.replace(/\s+/g, ' ').replace(/,\s*/g, ',').replace(/\s*,/g, ',').replace(/\s+$/, '');
}

const boilerplatePrefix = `select etrans.entity_shard,  etrans.entity_realm, etrans.entity_num , t.memo , t.consensus_ns , valid_start_ns ,
    coalesce(ttr.result, 'UNKNOWN') as result , coalesce(ttt.name, 'UNKNOWN') as name , t.fk_node_acc_id ,
    enode.entity_realm as node_realm , enode.entity_num as node_num,
    ctl.realm_num as account_realm, ctl.entity_num as account_num,
    amount , t.charged_tx_fee, t.valid_duration_seconds, t.max_fee
from ( select distinct ctl.consensus_timestamp
    from t_cryptotransferlists ctl
    join t_transactions t on t.consensus_ns = ctl.consensus_timestamp `;

const boilerplateSufffix = ` join t_transactions t on tlist.consensus_timestamp = t.consensus_ns
    left outer join t_transaction_results ttr on ttr.proto_id = t.result
    join t_entities enode on enode.id = t.fk_node_acc_id
    join t_entities etrans on etrans.id = t.fk_payer_acc_id
    left outer join t_transaction_types ttt on ttt.proto_id = t.type
    left outer join t_cryptotransferlists ctl on tlist.consensus_timestamp = ctl.consensus_timestamp
    order by t.consensus_ns desc,account_num asc,amount asc`;

test('transactions by timestamp gte', () => {
  let sql = transactions.reqToSql({query: {timestamp: 'gte:1234'}});
  let expected = normalizeSql(
    boilerplatePrefix +
      `where 1=1
         and ((t.consensus_ns  >=  $1) ) and 1=1   order by ctl.consensus_timestamp desc limit $2 ) as tlist` +
      boilerplateSufffix
  );
  expect(normalizeSql(sql.query)).toEqual(expected);
  expect(sql.params).toEqual(['1234000000000', 1000]);
});

test('transactions by timestamp eq', () => {
  let sql = transactions.reqToSql({query: {timestamp: 'eq:123'}});
  let expected = normalizeSql(
    boilerplatePrefix +
      `where 1=1
        and ((t.consensus_ns  =  $1) ) and 1=1   order by ctl.consensus_timestamp desc limit $2 ) as tlist` +
      boilerplateSufffix
  );
  expect(normalizeSql(sql.query)).toEqual(expected);
  expect(sql.params).toEqual(['123000000000', 1000]);
});

test('transactions by account eq', () => {
  let sql = transactions.reqToSql({query: {'account.id': '0.1.123'}});
  let expected = normalizeSql(
    boilerplatePrefix +
      ` where (realm_num  =  $1 and entity_num  =  $2 )` +
      `and 1=1 and 1=1   order by ctl.consensus_timestamp desc
        limit $3 ) as tlist` +
      boilerplateSufffix
  );
  expect(normalizeSql(sql.query)).toEqual(expected);
  expect(sql.params).toEqual(['1', '123', 1000]);
});
