package de.kaufhof.pillar

import java.util.Date

import com.datastax.driver.core.Session

trait Reporter {
  def initializing(session: Session, keyspace: String, replicationStrategy: ReplicationStrategy)
  def migrating(session: Session, dateRestriction: Option[Date])
  def applying(migration: Migration)
  def reversing(migration: Migration)
  def destroying(session: Session, keyspace: String)
}