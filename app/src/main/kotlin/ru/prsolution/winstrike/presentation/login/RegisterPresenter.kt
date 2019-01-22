package ru.prsolution.winstrike.presentation.login

import ru.prsolution.winstrike.datasource.model.ConfirmSmsModel
import ru.prsolution.winstrike.domain.models.LoginModel
import ru.prsolution.winstrike.domain.models.MessageResponse
import ru.prsolution.winstrike.mvp.views.RegisterView
import ru.prsolution.winstrike.networking.NetworkError
import ru.prsolution.winstrike.networking.Service
import rx.subscriptions.CompositeSubscription

class RegisterPresenter(private val service: Service?, private val view: RegisterView) {
	private val subscriptions: CompositeSubscription

	init {
		this.subscriptions = CompositeSubscription()
	}

	fun createUser(user: LoginModel) {

/*		val subscription = service.createUser(object : Service.RegisterCallback {
			override fun onSuccess(authResponse: AuthResponse) {
				view.onRegisterSuccess(authResponse)
			}

			override fun onError(networkError: NetworkError) {
				view.onRegisterFailure(networkError.appErrorMessage)
			}

		}, user)

		subscriptions.add(subscription)*/
	}


	/**
	 * Send code by tap on "Again button send"
	 * @param smsModel
	 */
	fun sendSms(smsModel: ConfirmSmsModel) {

		val subscription = service?.sendSmsByUserRequest(object : Service.SmsCallback {
			override fun onSuccess(authResponse: MessageResponse) {
				view.onSendSmsSuccess(authResponse)
			}

			override fun onError(networkError: NetworkError) {
				view.onSmsSendFailure(networkError.appErrorMessage)
			}

		}, smsModel)

		subscriptions.add(subscription)
	}


	fun onStop() {
		subscriptions.unsubscribe()
	}

}
