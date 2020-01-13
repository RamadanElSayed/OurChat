package com.example.ourchat.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.ourchat.R
import com.example.ourchat.Utils.AuthUtil
import com.example.ourchat.Utils.ErrorMessage
import com.example.ourchat.Utils.LoadState
import com.example.ourchat.Utils.eventbus_events.KeyboardEvent
import com.example.ourchat.databinding.SignupFragmentBinding
import kotlinx.android.synthetic.main.issue_layout.view.*
import org.greenrobot.eventbus.EventBus
import java.util.regex.Matcher
import java.util.regex.Pattern


class SignupFragment : Fragment() {

    private lateinit var binding: SignupFragmentBinding

    companion object {
        fun newInstance() = SignupFragment()
    }

    private lateinit var viewModel: SignupViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.signup_fragment, container, false)
        return binding.root
    }





    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SignupViewModel::class.java)


        //regex pattern to check email format
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$"
        val pattern: Pattern = Pattern.compile(emailRegex)


        //handle register click
        binding.registerButton.setOnClickListener {

            EventBus.getDefault().post(KeyboardEvent())

            binding.userName.isErrorEnabled = false
            binding.email.isErrorEnabled = false
            binding.password.isErrorEnabled = false


            if (binding.userName.editText!!.text.length < 4) {
                binding.userName.error = "User name should be at least 4 characters"
                return@setOnClickListener
            }


            //check if email is empty or wrong format
            if (!binding.email.editText!!.text.isEmpty()) {
                val matcher: Matcher = pattern.matcher(binding.email.editText!!.text)
                if (!matcher.matches()) {
                    binding.email.error = "Email format isn't correct."
                    return@setOnClickListener
                }
            } else if (binding.email.editText!!.text.isEmpty()) {
                binding.email.error = "Email field can't be empty."
                return@setOnClickListener
            }


            if (binding.password.editText!!.text.length < 6) {
                binding.password.error = "Password should be at least 6 characters"
                return@setOnClickListener
            }

            //email and pass are matching requirements now we can register to firebase auth

            viewModel.registerEmail(
                AuthUtil.firebaseAuthInstance,
                binding.email.editText!!.text.toString(),
                binding.password.editText!!.text.toString(),
                binding.userName.editText!!.text.toString()
            ).observe(this, Observer { authUser ->
                //authentication success and we should save user in firestore
                viewModel.storeUserInFirestore(authUser)
                    .observe(this, Observer {
                        // user is stored in firebase
                        this@SignupFragment.findNavController()
                            .navigate(R.id.action_signupFragment_to_homeFragment)
                        Toast.makeText(context, "Sign up successful", Toast.LENGTH_LONG).show()
                    })
            })

        }


        //hide issue layout on x icon click
        binding.issueLayout.cancelImage.setOnClickListener {
            binding.issueLayout.visibility = View.GONE
        }

        //show proper loading/error ui
        viewModel.loadingState.observe(this, Observer {
            when (it) {
                LoadState.LOADING -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.issueLayout.visibility = View.GONE
                }
                LoadState.SUCCESS -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.visibility = View.GONE
                }
                LoadState.FAILURE -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.visibility = View.VISIBLE
                    binding.issueLayout.textViewIssue.text = ErrorMessage.errorMessage
                }

            }
        })
    }





}



