package scala.meta
package internal
package utils

import scala.scalajs.js

class WeakHashMap[K <: AnyRef, T] {

  private val inner = WeakMap[K, T]()

  def update(key: K, value: T): Unit = inner.set(key, value)

  def get(key: K): Option[T] = {
    val res = inner.get(key)
    if (js.isUndefined(res)) None
    else Some(res)
  }
}

object  WeakHashMap {

  def apply[K <: AnyRef, T]() = new utils.WeakHashMap[K, T]()

}

import js.annotation.JSBracketAccess

@js.native
sealed trait WeakMap[K <: AnyRef, V] extends js.Any {

  @JSBracketAccess
  def get(key: K): V = js.native

  @JSBracketAccess
  def set(key: K, value: V): Unit = js.native

}

object WeakMap {
  def empty[K <: AnyRef, V]: WeakMap[K, V] = (new Object).asInstanceOf[WeakMap[K, V]]

  def apply[K <: AnyRef, V](): WeakMap[K, V] = empty[K, V]
}
