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
  - [ ] 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
  - [ ] 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
  - [ ] 동시성 제어에 대한 통합 테스트 작성
- Step2
  - [ ] 동시성 제어 방식에 대한 분석 및 보고서 작성 ( README.md )
 
## 동시성 제어 방식에 대한 분석 및 보고서
