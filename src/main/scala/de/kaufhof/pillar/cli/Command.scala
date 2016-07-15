package de.kaufhof.pillar.cli

import de.kaufhof.pillar.{Registry, ReplicationStrategy}
import com.datastax.driver.core.Session

case class Command(action: MigratorAction, session: Session, keyspace: String, timeStampOption: Option[Long], registry: Registry, replicationStrategy: ReplicationStrategy)