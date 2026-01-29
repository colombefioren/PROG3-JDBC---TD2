package com.jdbctd2.repository.interf;

import com.jdbctd2.model.Table;
import java.time.Instant;
import java.util.List;

public interface TableRepository {
  Table findById(Integer id);

  Table findByNumber(Integer number);

  List<Table> findAll();

  List<Integer> findAvailableTableNumbersAt(Instant instant);

  List<Table> findAvailableTablesAt(Instant instant);
}
