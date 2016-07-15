package de.kaufhof.pillar

import java.util.Date

import com.datastax.driver.core.Session

class ReportingMigrator(reporter: Reporter, wrapped: Migrator) extends Migrator {
  override def initialize(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy) {
    reporter.initializing(session, keyspace, replicationStrategy)
    wrapped.initialize(session, keyspace, replicationStrategy)
  }

  override def migrate(session: Session, dateRestriction: Option[Date] = None) {
    reporter.migrating(session, dateRestriction)
    wrapped.migrate(session, dateRestriction)
  }

  override def destroy(session: Session, keyspace: String) {
    reporter.destroying(session, keyspace)
    wrapped.destroy(session, keyspace)
  }
}