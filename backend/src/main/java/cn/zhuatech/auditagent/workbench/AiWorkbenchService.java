/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.auditagent.workbench;

import static cn.zhuatech.auditagent.workbench.WorkbenchInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);
    keys(r, "seed", "rate", "records");
    String seed = text(r, "seed", 200);
    BigDecimal rate = decimal(r, "rate", 0.000001, 1);
    Set<String> ids = new HashSet<>();
    Map<String, List<String>> groups = new TreeMap<>();
    for (var row : rows(r, "records", 1, 500)) {
      keys(row, "id", "stratum");
      String id = id(row, "id");
      unique(ids, id);
      groups.computeIfAbsent(id(row, "stratum"), k -> new ArrayList<>()).add(id);
    }
    List<Map<String, Object>> selected = new ArrayList<>(), strata = new ArrayList<>();
    for (var entry : groups.entrySet()) {
      var group = entry.getValue();
      group.sort(
          Comparator.<String, String>comparing(id -> digest(seed + "\n" + id))
              .thenComparing(id -> id));
      int count =
          Math.max(
              1,
              BigDecimal.valueOf(group.size())
                  .multiply(rate)
                  .setScale(0, RoundingMode.CEILING)
                  .intValueExact());
      for (int i = 0; i < count; i++)
        selected.add(
            Map.of(
                "id",
                group.get(i),
                "stratum",
                entry.getKey(),
                "selectionDigest",
                digest(seed + "\n" + group.get(i))));
      strata.add(
          Map.of("stratum", entry.getKey(), "population", group.size(), "sampleSize", count));
    }
    return Map.of(
        "population",
        ids.size(),
        "sampleSize",
        selected.size(),
        "selected",
        selected,
        "strata",
        strata,
        "method",
        "SHA256_ORDER_WITH_CEILING");
  }
}
