package lostvaults.server
/**
 * Logger.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
import akka.event.Logging.InitializeLogger
import akka.event.Logging.Error
import akka.event.Logging.Warning
import akka.event.Logging.Info
import akka.event.Logging.Debug
import akka.actor.Actor
import java.io._
import java.util.Calendar
import akka.actor.actorRef2Scala
import akka.event.Logging.LoggerInitialized

class Logger extends Actor {
  import akka.event.Logging._
  val op: FileOutputStream = new FileOutputStream("server.log", false)
  val writer = new PrintWriter(op)
  val calendar = Calendar.getInstance()
    
  override def postStop() = {
    writer.close
    op.close
  }
  def receive() = {
    case InitializeLogger(_) => {
      sender ! LoggerInitialized
    }
    case Error(cause, logSource, logClass, message) => {
      val hour = calendar.get(Calendar.HOUR)
      val minute = calendar.get(Calendar.MINUTE)
      val tStamp = "[" + hour + ":" + minute + "]"
      val log = tStamp + " ERROR: \"" + message + "\" (\"" + cause + "\") from " + logSource + "\n--\n"
      writer.write(log)
      writer.flush()
    }
    case Warning(logSource, logClass, message) => {
      val hour = calendar.get(Calendar.HOUR)
      val minute = calendar.get(Calendar.MINUTE)
      val tStamp = "[" + hour + ":" + minute + "]"
      val log = tStamp + " WARNING: \"" + message + "\" from " + logSource + "\n--\n"
      writer.write(log)
      writer.flush()
    }
    case Info(logSource, logClass, message) => {
      val hour = calendar.get(Calendar.HOUR)
      val minute = calendar.get(Calendar.MINUTE)
      val tStamp = "[" + hour + ":" + minute + "]"
      val log = tStamp + " INFO: \"" + message + "\" from " + logSource + "\n--\n"
      writer.write(log)
      writer.flush()
    }
    case Debug(logSource, logClass, message) => {
      val hour = calendar.get(Calendar.HOUR)
      val minute = calendar.get(Calendar.MINUTE)
      val tStamp = "[" + hour + ":" + minute + "]"
      val log = tStamp + " DEBUG: \"" + message + "\" from " + logSource + "\n---\n"
      writer.write(log)
      writer.flush()
    }
  }
}
