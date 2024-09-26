# HH99-Week01-TDD
[ 1주차 과제 ] TDD 로 개발하기 

## 🔥 TODO
- Default
  - [X] `/point` 패키지 (디렉토리) 내에 `PointService` 기본 기능 작성
  - [X] `/database` 패키지의 구현체는 수정하지 않고, 이를 활용해 기능을 구현
  - [X] PATCH  `/point/{id}/charge` : 포인트를 충전한다.
  - [X] PATCH `/point/{id}/use` : 포인트를 사용한다.
  - [X] GET `/point/{id}` : 포인트를 조회한다.
  - [X] GET `/point/{id}/histories` : 포인트 내역을 조회한다.
- Step1
  - [X] 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
  - [X] 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
  - [X] 동시성 제어에 대한 통합 테스트 작성
- Step2
  - [ ] 동시성 제어 방식에 대한 분석 및 보고서 작성 ( README.md )
 
## 동시성 제어 방식에 대한 분석 및 보고서
PointService 클래스는 다음과 같은 두 가지 주요 동작을 수행합니다:

- 포인트 충전 (`charge()`): 사용자의 포인트를 증가시킴
- 포인트 사용 (`use()`): 사용자의 포인트를 감소시킴

``이 두 메서드는 사용자별 포인트 잔액을 수정하기 때문에 동시에 여러 쓰레드가 동일한 사용자의 포인트에 접근할 경우 데이터 불일치 문제가 발생할 수 있습니다.``

### 동시성 제어 방식

동시성 문제를 해결하기 위해 PointService에서는 ReentrantLock을 사용하여 자원(사용자의 포인트)에 대한 접근을 제어하고 있습니다.
이 방식은 동일한 사용자에 대해 charge() 또는 use() 메서드가 호출될 때,
하나의 쓰레드만 자원에 접근하도록 보장합니다.

### ReentrantLock의 사용

`charge()` 및 `use()` 메서드에서는 각각 `lock.lock()`을 통해 락을 획득하고, 메서드가 끝난 후에는 `finally` 블록에서 `lock.unlock()`을 호출하여 락을 해제합니다.
이는 Java의 ReentrantLock을 사용한 전형적인 동시성 제어 방식으로, 예외가 발생하거나 정상 종료되는 경우에도 항상 락이 해제되도록 보장합니다

- `ConcurrentHashMap`과 같은 구조를 사용해 각 사용자 ID별로 락을 관리함으로써, 동일한 사용자에 대한 요청만 동시성 제어를 하고 다른 사용자는 별도로 처리될 수 있습니다.

