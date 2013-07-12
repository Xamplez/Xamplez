package utils

import annotation.tailrec
import java.util.concurrent.atomic.AtomicReference
 
class Atomic[T](val atomic : AtomicReference[T]) {
  
  @tailrec
  final def update(f: T => T) : T = {
    val oldValue = atomic.get()
    val newValue = f(oldValue)
    if (atomic.compareAndSet(oldValue, newValue)) newValue else update(f)
  }

}

object Atomic {

  def apply[T]( obj : T ) = new Atomic(new AtomicReference(obj))
  
  implicit def toAtomic[T]( ref : AtomicReference[T]) : Atomic[T] = new Atomic(ref)

  implicit def delegateToAtomicReference[T]( a: Atomic[T] ) = a.atomic

}