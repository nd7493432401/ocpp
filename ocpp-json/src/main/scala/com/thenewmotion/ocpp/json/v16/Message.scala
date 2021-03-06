package com.thenewmotion.ocpp.json.v16

import java.time.ZonedDateTime

import com.thenewmotion.ocpp.json.VersionSpecificMessage

// we use fieldless case classes instead of case objects because that plays nicer with lift-json

sealed trait Message extends VersionSpecificMessage

sealed trait CentralSystemReq extends Message
sealed trait CentralSystemRes extends Message

case class AuthorizeReq(idTag: String) extends CentralSystemReq
case class AuthorizeRes(idTagInfo: IdTagInfo) extends CentralSystemRes

case class BootNotificationReq(
  chargePointVendor: String,
  chargePointModel: String,
  chargePointSerialNumber: Option[String],
  chargeBoxSerialNumber: Option[String],
  firmwareVersion: Option[String],
  iccid: Option[String],
  imsi: Option[String],
  meterType: Option[String],
  meterSerialNumber: Option[String]
) extends CentralSystemReq
case class BootNotificationRes(
  status: String,
  currentTime: ZonedDateTime,
  interval: Int
) extends CentralSystemRes

case class DiagnosticsStatusNotificationReq(status: String) extends CentralSystemReq
case class DiagnosticsStatusNotificationRes() extends CentralSystemRes

case class FirmwareStatusNotificationReq(status: String) extends CentralSystemReq
case class FirmwareStatusNotificationRes() extends CentralSystemRes

case class HeartbeatReq() extends CentralSystemReq
case class HeartbeatRes(currentTime: ZonedDateTime) extends CentralSystemRes

case class MeterValuesReq(
  connectorId: Int,
  transactionId: Option[Int],
  meterValue: List[Meter]
) extends CentralSystemReq
case class MeterValuesRes() extends CentralSystemRes

case class StartTransactionReq(
  connectorId: Int,
  idTag: String,
  timestamp: ZonedDateTime,
  meterStart: Int,
  reservationId: Option[Int]
) extends CentralSystemReq
case class StartTransactionRes(
  transactionId: Int,
  idTagInfo: IdTagInfo
) extends CentralSystemRes

case class StatusNotificationReq(
  connectorId: Int,
  status: String,
  errorCode: String,
  info: Option[String],
  timestamp: Option[ZonedDateTime],
  vendorId: Option[String],
  vendorErrorCode: Option[String]
) extends CentralSystemReq
case class StatusNotificationRes() extends CentralSystemRes

case class StopTransactionReq(
  transactionId: Int,
  idTag: Option[String],
  timestamp: ZonedDateTime,
  meterStop: Int,
  reason: Option[String],
  transactionData: Option[List[Meter]]
) extends CentralSystemReq
case class StopTransactionRes(
  idTagInfo: Option[IdTagInfo]
) extends CentralSystemRes


sealed trait ChargePointReq extends Message
sealed trait ChargePointRes extends Message

case class CancelReservationReq(reservationId: Int) extends ChargePointReq
case class CancelReservationRes(status: String) extends ChargePointRes

case class ChangeAvailabilityReq(connectorId: Int, `type`: String) extends ChargePointReq
case class ChangeAvailabilityRes(status: String) extends ChargePointRes

case class ChangeConfigurationReq(key: String, value: String) extends ChargePointReq
case class ChangeConfigurationRes(status: String) extends ChargePointRes

case class ClearCacheReq() extends ChargePointReq
case class ClearCacheRes(status: String) extends ChargePointRes

case class ClearChargingProfileReq(
  id: Option[Int],
  connectorId: Option[Int],
  chargingProfilePurpose: Option[String],
  stackLevel: Option[Int]
) extends ChargePointReq
case class ClearChargingProfileRes(status: String) extends ChargePointRes

case class DataTransferReq(
  vendorId: String,
  messageId: Option[String],
  data: Option[String]
) extends CentralSystemReq with ChargePointReq
case class DataTransferRes(
  status: String,
  data: Option[String]
) extends CentralSystemRes with ChargePointRes

case class GetCompositeScheduleReq(
  connectorId: Int,
  duration: Int,
  chargingRateUnit: Option[String]
) extends ChargePointReq
case class GetCompositeScheduleRes(
  status: String,
  connectorId: Option[Int],
  scheduleStart: Option[ZonedDateTime],
  chargingSchedule: Option[ChargingSchedule]
) extends ChargePointRes

case class GetConfigurationReq(key: Option[List[String]]) extends ChargePointReq
case class GetConfigurationRes(
  configurationKey: Option[List[ConfigurationEntry]],
  unknownKey: Option[List[String]]
) extends ChargePointRes

case class GetDiagnosticsReq(
  location: String,
  startTime: Option[ZonedDateTime],
  stopTime: Option[ZonedDateTime],
  retries: Option[Int],
  retryInterval: Option[Int]
) extends ChargePointReq
case class GetDiagnosticsRes(fileName: Option[String]) extends ChargePointRes

case class GetLocalListVersionReq() extends ChargePointReq
case class GetLocalListVersionRes(listVersion: Int) extends ChargePointRes

case class RemoteStartTransactionReq(
  idTag: String,
  connectorId: Option[Int],
  chargingProfile: Option[ChargingProfile]
) extends ChargePointReq
case class RemoteStartTransactionRes(status: String) extends ChargePointRes

case class RemoteStopTransactionReq(transactionId: Int) extends ChargePointReq
case class RemoteStopTransactionRes(status: String) extends ChargePointRes

case class ReserveNowReq(
  connectorId: Int,
  expiryDate: ZonedDateTime,
  idTag: String,
  parentIdTag: Option[String],
  reservationId: Int
) extends ChargePointReq
case class ReserveNowRes(status: String) extends ChargePointRes

case class ResetReq(`type`: String) extends ChargePointReq
case class ResetRes(status: String) extends ChargePointRes

case class SendLocalListReq(
  updateType: String,
  listVersion: Int,
  localAuthorizationList: Option[List[AuthorisationData]]
) extends ChargePointReq
case class SendLocalListRes(status: String) extends ChargePointRes

case class SetChargingProfileReq(
  connectorId: Int,
  csChargingProfiles: ChargingProfile
) extends ChargePointReq
case class SetChargingProfileRes(
  status: String
) extends ChargePointRes

case class TriggerMessageReq(
  requestedMessage: String,
  connectorId: Option[Int]
) extends ChargePointReq
case class TriggerMessageRes(status: String) extends ChargePointRes

case class UnlockConnectorReq(connectorId: Int) extends ChargePointReq
case class UnlockConnectorRes(status: String) extends ChargePointRes

case class UpdateFirmwareReq(
  retrieveDate: ZonedDateTime,
  location: String,
  retries: Option[Int],
  retryInterval: Option[Int]
) extends ChargePointReq
case class UpdateFirmwareRes() extends ChargePointRes


case class IdTagInfo(
  status: String,
  expiryDate: Option[ZonedDateTime],
  parentIdTag: Option[String]
)

case class Meter(timestamp: ZonedDateTime, sampledValue: List[MeterValue])
case class MeterValue(
  value: String,
  context: Option[String],
  format: Option[String],
  measurand: Option[String],
  phase: Option[String],
  location: Option[String],
  unit: Option[String]
)

case class ConfigurationEntry(
  key: String,
  readonly: Boolean,
  value: Option[String]
)
case class AuthorisationData(idTag: String, idTagInfo: Option[IdTagInfo])

case class ChargingProfile(
  chargingProfileId: Int,
  stackLevel: Int,
  chargingProfilePurpose: String,
  chargingProfileKind: String,
  chargingSchedule: ChargingSchedule,
  transactionId: Option[Int],
  recurrencyKind: Option[String],
  validFrom: Option[ZonedDateTime],
  validTo: Option[ZonedDateTime]
)

case class ChargingSchedule(
  chargingRateUnit: String,
  chargingSchedulePeriod: List[ChargingSchedulePeriod],
  duration: Option[Int],
  startSchedule: Option[ZonedDateTime],
  minChargingRate: Option[Float]
)

case class ChargingSchedulePeriod(
  startPeriod: Int,
  limit: Float,
  numberPhases: Option[Int]
)
